package com.generated.orion.common.infrastructure.layout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Load XML layout (format: {@code <dataLayout>...<group>/<field>}) → {@link RecordSchema} (FD
 * scope) or {@link StorageSchema} (WS scope).
 *
 * <p>Performs XSD validation at load time (data-layout.xsd on the classpath).
 *
 * <p>Multi-buffer WS: WS XML has multiple top-level groups (1 per 01-level). Loose fields at root
 * level are wrapped into a synthetic group "WS-SYS".
 */
public final class LayoutLoader {

    private static final String XSD_PATH = "/layout/data-layout.xsd";
    private static final Logger LOG = Logger.getLogger(LayoutLoader.class.getName());

    private LayoutLoader() {}

    /**
     * Initialization-on-demand holder: lazy, thread-safe XSD load guaranteed by the JLS class-init
     * lock — no volatile field, no explicit synchronization (avoids S3077).
     */
    private static final class XsdHolder {
        static final Schema SCHEMA = load();

        private static Schema load() {
            try (InputStream xsdIs = LayoutLoader.class.getResourceAsStream(XSD_PATH)) {
                if (xsdIs == null) return null; // graceful degrade
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                // XXE hardening (S2755): disable external DTD and schema resolution. An
                // unsupported-property SAXException is absorbed by the surrounding handler
                // and validation is then skipped (graceful degrade).
                sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                return sf.newSchema(new javax.xml.transform.stream.StreamSource(xsdIs));
            } catch (Exception e) {
                LOG.warning("XSD load failed, skip validation: " + e.getMessage());
                return null;
            }
        }
    }

    private static Schema getXsdSchema() {
        return XsdHolder.SCHEMA;
    }

    /* ── FILE scope (FD) ─────────────────────────────────────────────── */

    public static RecordSchema loadFile(String classpathResource) {
        InputStream is = LayoutLoader.class.getResourceAsStream(classpathResource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + classpathResource);
        }
        try {
            return loadFile(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                /* best-effort close */
            }
        }
    }

    public static RecordSchema loadFile(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return loadFile(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static RecordSchema loadFile(InputStream is) {
        Element root = parseRoot(is);
        String scope = attr(root, "scope");
        if (scope != null && !scope.isEmpty() && !"file".equalsIgnoreCase(scope)) {
            throw new IllegalArgumentException(
                    "loadFile() requires scope=\"file\"; got scope=\""
                            + scope
                            + "\". Use loadWorkingStorage() instead.");
        }
        String name = attr(root, "name");
        Charset cs = parseCharset(root);
        boolean csv = "true".equalsIgnoreCase(attr(root, "csv"));

        // FD with multiple record alternates (e.g. HCNTRL has CTL-REC + CTL01-REC..CTL05-REC,
        // each sharing the same byte buffer per COBOL spec) emits as: first <group> primary,
        // subsequent <group redefines="..."> siblings. Attach redefiners to primary so
        // RecordSchema.byName indexes all alternate fields and FieldRouter can resolve them.
        SchemaGroup primary = null;
        List<SchemaGroup> redefiners = new ArrayList<>();
        for (Element child : childElements(root)) {
            if (!"group".equalsIgnoreCase(child.getTagName())) {
                continue;
            }
            SchemaGroup g = parseGroup(child);
            if (g.getRedefinesName() != null && !g.getRedefinesName().isEmpty()) {
                redefiners.add(g);
            } else if (primary == null) {
                primary = g;
            } else {
                throw new IllegalArgumentException(
                        "FD '"
                                + name
                                + "' has multiple non-REDEFINES top-level <group>s; subsequent"
                                + " record alternates must declare"
                                + " redefines=\"<first-record-name>\".");
            }
        }
        if (primary == null) {
            throw new IllegalArgumentException(
                    "<dataLayout> must contain a child <group> (file scope)");
        }
        for (SchemaGroup redef : redefiners) {
            redef.parent = primary;
            primary.children.add(redef);
        }
        return new RecordSchema(name, cs, primary, csv);
    }

    /* ── WORKING-STORAGE scope ───────────────────────────────────────── */

    public static StorageSchema loadWorkingStorage(String classpathResource) {
        InputStream is = LayoutLoader.class.getResourceAsStream(classpathResource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + classpathResource);
        }
        try {
            return loadWorkingStorage(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                /* best-effort close */
            }
        }
    }

    public static StorageSchema loadWorkingStorage(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return loadWorkingStorage(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StorageSchema loadWorkingStorage(InputStream is) {
        Element root = parseRoot(is);
        String scope = attr(root, "scope");
        if (!"workingStorage".equalsIgnoreCase(scope)) {
            throw new IllegalArgumentException(
                    "loadWorkingStorage() requires scope=\"workingStorage\"; got scope=\""
                            + scope
                            + "\". Use loadFile() instead.");
        }
        String name = attr(root, "name");
        Charset cs = parseCharset(root);

        // Build N buffer-roots: direct children of <dataLayout>
        List<RecordSchema> buffers = new ArrayList<>();
        List<Element> looseFields = new ArrayList<>();

        // Legacy-wrapper unwrap: if root has exactly 1 <group> child wrapping 2+ groups,
        // treat that single child as the wrapper and use its children as buffer-roots.
        // Reached only for stale XML with an outer wrapper element.
        List<Element> rootChildren = childElements(root);
        if (rootChildren.size() == 1
                && "group".equalsIgnoreCase(rootChildren.get(0).getTagName())) {
            // Could be legacy outer wrapper. Heuristic: if it has 2+ group children, treat as
            // wrapper.
            List<Element> inner = childElements(rootChildren.get(0));
            int groupCount = 0;
            for (Element e : inner) {
                if ("group".equalsIgnoreCase(e.getTagName())) groupCount++;
            }
            if (groupCount >= 2) {
                rootChildren = inner;
            }
        }

        // A top-level <group redefines="X"> must share buffer with X's container, not become
        // its own RecordSchema — otherwise byName lookup for X fails because each primary
        // group has its own isolated byName map. Split primaries vs redefiners.
        List<SchemaGroup> primaryGroups = new ArrayList<>();
        List<SchemaGroup> redefiningGroups = new ArrayList<>();
        for (Element child : rootChildren) {
            String tag = child.getTagName().toLowerCase();
            if ("group".equals(tag)) {
                SchemaGroup g = parseGroup(child);
                if (g.getRedefinesName() != null && !g.getRedefinesName().isEmpty()) {
                    redefiningGroups.add(g);
                } else {
                    primaryGroups.add(g);
                }
            } else if ("field".equals(tag)) {
                looseFields.add(child);
            } else {
                throw new IllegalArgumentException("Unsupported tag at WS root: " + tag);
            }
        }

        // Synthetic WS-SYS group for any loose top-level fields (COMPLETION-CODE, INDEX names, ...)
        if (!looseFields.isEmpty()) {
            SchemaGroup sys = new SchemaGroup("WS-SYS", null);
            sys.byteOffset = 0;
            int cumOffset = 0;
            for (Element fe : looseFields) {
                LayoutField f = parseField(fe);
                // Pack sequential within SYS group when offset wasn't explicit (-1 sentinel).
                if (f.getByteOffset() < 0) {
                    f.byteOffset = cumOffset;
                }
                cumOffset = Math.max(cumOffset, f.getByteOffset() + f.getByteLength());
                f.parent = sys;
                sys.children.add(f);
            }
            sys.byteLength = cumOffset;
            primaryGroups.add(sys);
        }

        // Attach each redefining top-level group to the primary group that contains its target,
        // so they share a RecordSchema (and therefore share byName for REDEFINES resolution).
        // A target may itself be a redefining group (REDEFINES of a REDEFINES — e.g.
        // `01 FILLER REDEFINES CTRTLIAO` where CTRTLIAO REDEFINES CTRTLIAI). findContainingGroup
        // searches the whole primary tree including already-attached redefines children, so such a
        // group becomes resolvable once its target has itself been attached. Resolve by fixpoint:
        // repeatedly attach whatever is now resolvable until a full pass makes no progress.
        List<SchemaGroup> pending = new ArrayList<>(redefiningGroups);
        boolean progress = true;
        while (progress && !pending.isEmpty()) {
            progress = false;
            List<SchemaGroup> stillPending = new ArrayList<>();
            for (SchemaGroup redef : pending) {
                String targetName = redef.getRedefinesName();
                SchemaGroup container = null;
                for (SchemaGroup p : primaryGroups) {
                    container = findContainingGroup(p, targetName);
                    if (container != null) {
                        break;
                    }
                }
                if (container != null) {
                    redef.parent = container;
                    container.children.add(redef);
                    progress = true;
                } else {
                    stillPending.add(redef);
                }
            }
            pending = stillPending;
        }
        if (!pending.isEmpty()) {
            SchemaGroup redef = pending.get(0);
            throw new IllegalStateException(
                    "REDEFINES target group not found across WS top-level: '"
                            + redef.getRedefinesName()
                            + "' for redefining group '"
                            + redef.getName()
                            + "'");
        }

        for (SchemaGroup p : primaryGroups) {
            buffers.add(new RecordSchema(p.getName(), cs, p));
        }

        return new StorageSchema(name, cs, buffers);
    }

    /**
     * Find the group to attach a REDEFINES sibling under, given the target node's name. - If a
     * group's NAME matches targetName, return that group (target is a top-level 01-level — the
     * redefining group overlays it from offset 0 and becomes its child). - Else if a child node's
     * name matches, return the group containing it.
     */
    private static SchemaGroup findContainingGroup(SchemaGroup group, String targetName) {
        if (targetName.equals(group.name)) {
            return group;
        }
        for (SchemaNode child : group.children) {
            if (targetName.equals(child.name)) {
                return group;
            }
            if (child instanceof SchemaGroup cg) {
                SchemaGroup found = findContainingGroup(cg, targetName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /* ── shared parse helpers ────────────────────────────────────────── */

    private static Element parseRoot(InputStream is) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setIgnoringElementContentWhitespace(true);
            try {
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (Exception ignore) {
                /* best-effort: parser may not support feature */
            }
            try {
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            } catch (Exception ignore) {
                /* best-effort: parser may not support feature */
            }
            try {
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            } catch (Exception ignore) {
                /* best-effort: parser may not support feature */
            }
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();
            if (!"dataLayout".equalsIgnoreCase(root.getTagName())) {
                throw new IllegalArgumentException(
                        "XML root must be <dataLayout>, got: " + root.getTagName());
            }
            Schema schema = getXsdSchema();
            if (schema != null) {
                try {
                    Validator v = schema.newValidator();
                    v.validate(new DOMSource(doc));
                } catch (SAXException sxe) {
                    throw new IllegalStateException(
                            "XSD validation FAILED cho <dataLayout name=\""
                                    + root.getAttribute("name")
                                    + "\">: "
                                    + sxe.getMessage(),
                            sxe);
                }
            }
            return root;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read dataLayout XML: " + e.getMessage(), e);
        }
    }

    private static Charset parseCharset(Element root) {
        String cs = attr(root, "charset");
        if (cs == null || cs.isEmpty()) {
            cs = "UTF-8";
        }
        return Charset.forName(cs);
    }

    private static SchemaGroup parseGroup(Element el) {
        SchemaGroup group = new SchemaGroup(attr(el, "name"), attr(el, "redefines"));
        group.setOccurs(attrInt(el, "occurs", 1));
        group.setElementSize(attrInt(el, "elementSize", 0));
        int gOffset = attrInt(el, "offset", -1);
        int gSize = attrInt(el, "size", -1);
        if (gOffset >= 0) {
            group.byteOffset = gOffset;
        }
        if (gSize >= 0) {
            group.byteLength = gSize;
        }
        // REDEFINES Flavor B: discriminator + variantWhen
        String disc = attr(el, "discriminator");
        if (disc != null && !disc.isEmpty()) {
            group.setDiscriminatorField(disc);
        }
        String variantWhen = attr(el, "variantWhen");
        if (variantWhen != null && !variantWhen.isEmpty()) {
            java.util.List<String> vs = new ArrayList<>();
            for (String tok : variantWhen.trim().split("\\s+")) {
                if (!tok.isEmpty()) {
                    vs.add(tok);
                }
            }
            if (!vs.isEmpty()) {
                group.setVariantWhen(vs);
            }
        }
        for (Element child : childElements(el)) {
            String tag = child.getTagName().toLowerCase();
            SchemaNode node;
            if ("group".equals(tag)) {
                node = parseGroup(child);
            } else if ("field".equals(tag)) node = parseField(child);
            else throw new IllegalArgumentException("Unsupported tag: " + tag);
            node.parent = group;
            group.children.add(node);
        }
        return group;
    }

    private static LayoutField parseField(Element el) {
        String name = attr(el, "name");
        String redefines = attr(el, "redefines");
        String javaType = attr(el, "javaType");
        // -1 sentinel = offset not explicit; caller (or RecordSchema.resolve) supplies it.
        // This lets resolve() distinguish "explicit offset 0" from "missing", required for
        // fields inside REDEFINES groups where emit omits offsets.
        int byteOffset = attrInt(el, "offset", -1);
        int byteLength = attrInt(el, "size", 0);
        boolean signed = "true".equalsIgnoreCase(attr(el, "signed"));
        int scale = attrInt(el, "scale", 0);
        String value = attr(el, "value");
        LayoutField.Usage usage = parseUsage(attr(el, "usage"));
        LayoutField.Kind kind = inferKind(javaType, usage);
        // COBOL edit-picture semantics. editPicture (raw PIC) is the display source of truth.
        String editPicture = attr(el, "editPicture");
        LayoutField.EditFormat editFormat = parseEditFormat(attr(el, "editFormat"));
        int editIntegerDigits = attrInt(el, "integerDigits", 0);
        boolean editInsertCommas = "true".equalsIgnoreCase(attr(el, "insertCommas"));
        boolean editHasTrailingNine = "true".equalsIgnoreCase(attr(el, "hasTrailingNine"));
        String editSlashAfter = attr(el, "slashAfter");
        LayoutField field =
                new LayoutField(
                        name,
                        redefines,
                        byteOffset,
                        byteLength,
                        kind,
                        usage,
                        signed,
                        scale,
                        javaType,
                        value,
                        editPicture,
                        editFormat,
                        editIntegerDigits,
                        editInsertCommas,
                        editHasTrailingNine,
                        editSlashAfter);
        field.setJustifiedRight("right".equalsIgnoreCase(attr(el, "justified")));
        return field;
    }

    private static LayoutField.EditFormat parseEditFormat(String s) {
        if (s == null || s.trim().isEmpty()) {
            return LayoutField.EditFormat.NONE;
        }
        try {
            return LayoutField.EditFormat.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LayoutField.EditFormat.NONE;
        }
    }

    private static LayoutField.Usage parseUsage(String s) {
        if (s == null || s.trim().isEmpty()) {
            return LayoutField.Usage.DISPLAY;
        }
        String u = s.trim().toUpperCase().replace("-", "");
        switch (u) {
            case "DISPLAY":
                return LayoutField.Usage.DISPLAY;
            case "COMP3", "PACKEDDECIMAL":
                return LayoutField.Usage.COMP3;
            case "COMP", "COMP4", "COMP5", "BINARY":
                return LayoutField.Usage.BINARY;
            case "COMP1", "COMPUTATIONAL1":
                return LayoutField.Usage.COMP1;
            case "COMP2", "COMPUTATIONAL2":
                return LayoutField.Usage.COMP2;
            default:
                throw new IllegalArgumentException("USAGE not supported: " + s);
        }
    }

    private static LayoutField.Kind inferKind(String javaType, LayoutField.Usage usage) {
        if (javaType == null) {
            return LayoutField.Kind.ALPHANUMERIC;
        }
        switch (javaType) {
            case "int", "long", "short", "BigDecimal", "float", "double", "Float", "Double":
                return LayoutField.Kind.NUM;
            case "NString":
                // PIC N (DBCS national) — route through encodeDbcs so
                // padding uses fullwidth-space (0x81 0x40) instead of ASCII (0x20).
                return LayoutField.Kind.DBCS;
            case "String":
            default:
                return LayoutField.Kind.ALPHANUMERIC;
        }
    }

    private static String attr(Element el, String key) {
        if (!el.hasAttribute(key)) {
            return null;
        }
        String v = el.getAttribute(key);
        return v.isEmpty() ? null : v;
    }

    private static int attrInt(Element el, String key, int defaultVal) {
        String v = attr(el, key);
        if (v == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ignore) {
            return defaultVal;
        }
    }

    private static Element firstChildElement(Element parent, String tagName) {
        for (Element c : childElements(parent)) {
            if (tagName.equalsIgnoreCase(c.getTagName())) {
                return c;
            }
        }
        return null;
    }

    private static List<Element> childElements(Element parent) {
        List<Element> out = new ArrayList<>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                out.add((Element) n);
            }
        }
        return out;
    }
}
