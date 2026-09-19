package com.sakura.runtime.record;

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
 * Parses record-schema XML ({@code <dataLayout>} with nested {@code <group>} / {@code <field>})
 * into a {@link RecordSchema} (file scope) or a {@link StorageSchema} (WORKING-STORAGE scope).
 *
 * <p>Documents are validated against record-schema.xsd from the classpath while loading.
 *
 * <p>A WORKING-STORAGE document carries one top-level group per 01-level item; loose fields at the
 * root are gathered into a synthetic "WS-SYS" group.
 */
public final class SchemaLoader {

    private static final String XSD_PATH = "/record-schema/record-schema.xsd";
    private static final Logger LOG = Logger.getLogger(SchemaLoader.class.getName());

    private SchemaLoader() {}

    /**
     * Holder idiom: the class-initialization lock yields a lazy, one-time, thread-safe XSD load
     * with no volatile field and no explicit locking.
     */
    private static final class XsdHolder {
        static final Schema SCHEMA = load();

        private static Schema load() {
            try (InputStream xsdIs = SchemaLoader.class.getResourceAsStream(XSD_PATH)) {
                if (xsdIs == null) return null; // validation becomes a no-op
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                // XXE hardening: external DTDs and schemas are switched off. Should the
                // parser reject the property, the surrounding catch downgrades to
                // running without validation.
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

    /* ── file records ────────────────────────────────────────────────── */

    public static RecordSchema loadFile(String classpathResource) {
        InputStream is = SchemaLoader.class.getResourceAsStream(classpathResource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + classpathResource);
        }
        try {
            return loadFile(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                /* close failure is inconsequential */
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

        // An FD declaring several record alternates shares one byte buffer among
        // them (COBOL rule). The XML carries the first <group> as the primary and
        // each further record as <group redefines="...">; the redefiners are hung
        // beneath the primary so every alternate field indexes into one schema
        // and stays resolvable.
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

    /* ── WORKING-STORAGE ─────────────────────────────────────────────── */

    public static StorageSchema loadWorkingStorage(String classpathResource) {
        InputStream is = SchemaLoader.class.getResourceAsStream(classpathResource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + classpathResource);
        }
        try {
            return loadWorkingStorage(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                /* close failure is inconsequential */
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

        // each direct child of <dataLayout> becomes one buffer root
        List<RecordSchema> buffers = new ArrayList<>();
        List<Element> looseFields = new ArrayList<>();

        // Older documents wrap everything in a single outer <group>; when that lone
        // child itself holds two or more groups, unwrap it and take its children as
        // the buffer roots.
        List<Element> rootChildren = childElements(root);
        if (rootChildren.size() == 1
                && "group".equalsIgnoreCase(rootChildren.get(0).getTagName())) {
            // two or more nested groups ⇒ the old wrapper shape
            List<Element> inner = childElements(rootChildren.get(0));
            int groupCount = 0;
            for (Element e : inner) {
                if ("group".equalsIgnoreCase(e.getTagName())) groupCount++;
            }
            if (groupCount >= 2) {
                rootChildren = inner;
            }
        }

        // A root-level <group redefines="X"> may not become its own schema: it has
        // to live in the buffer holding X, or lookups on the overlaid fields would
        // land in the wrong index. Primaries and redefiners are separated first.
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

        // loose root-level fields are collected under the synthetic WS-SYS group
        if (!looseFields.isEmpty()) {
            SchemaGroup sys = new SchemaGroup("WS-SYS", null);
            sys.byteOffset = 0;
            int cumOffset = 0;
            for (Element fe : looseFields) {
                SchemaField f = parseField(fe);
                // fields lacking an explicit offset are packed one after another
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

        // every redefining root group is attached under the primary containing its
        // target, so the two share one schema and REDEFINES resolution sees both
        for (SchemaGroup redef : redefiningGroups) {
            String targetName = redef.getRedefinesName();
            SchemaGroup container = null;
            for (SchemaGroup p : primaryGroups) {
                container = findContainingGroup(p, targetName);
                if (container != null) {
                    break;
                }
            }
            if (container == null) {
                throw new IllegalStateException(
                        "REDEFINES target group not found across WS top-level: '"
                                + targetName
                                + "' for redefining group '"
                                + redef.getName()
                                + "'");
            }
            redef.parent = container;
            container.children.add(redef);
        }

        for (SchemaGroup p : primaryGroups) {
            buffers.add(new RecordSchema(p.getName(), cs, p));
        }

        return new StorageSchema(name, cs, buffers);
    }

    /**
     * Group under which a REDEFINES sibling belongs, given the target's name: a group named like
     * the target hosts the overlay itself (the top-level 01 case — overlay starts at offset 0 and
     * becomes its child); otherwise the group directly containing a so-named child is returned.
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

    /* ── common XML helpers ──────────────────────────────────────────── */

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
                            "XSD validation FAILED for <dataLayout name=\""
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
        // flavor-B dispatch attributes
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

    private static SchemaField parseField(Element el) {
        String name = attr(el, "name");
        String redefines = attr(el, "redefines");
        String javaType = attr(el, "javaType");
        // -1 marks an absent offset, letting RecordSchema.resolve tell "explicitly 0"
        // apart from "not given" — REDEFINES members typically arrive without offsets.
        int byteOffset = attrInt(el, "offset", -1);
        int byteLength = attrInt(el, "size", 0);
        boolean signed = "true".equalsIgnoreCase(attr(el, "signed"));
        int scale = attrInt(el, "scale", 0);
        String value = attr(el, "value");
        SchemaField.Usage usage = parseUsage(attr(el, "usage"));
        SchemaField.Kind kind = inferKind(javaType, usage);
        // editing-picture attributes
        SchemaField.EditFormat editFormat = parseEditFormat(attr(el, "editFormat"));
        int editIntegerDigits = attrInt(el, "integerDigits", 0);
        boolean editInsertCommas = "true".equalsIgnoreCase(attr(el, "insertCommas"));
        boolean editHasTrailingNine = "true".equalsIgnoreCase(attr(el, "hasTrailingNine"));
        String editSlashAfter = attr(el, "slashAfter");
        String editCommaAfter = attr(el, "commaAfter");
        return new SchemaField(
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
                editFormat,
                editIntegerDigits,
                editInsertCommas,
                editHasTrailingNine,
                editSlashAfter,
                editCommaAfter);
    }

    private static SchemaField.EditFormat parseEditFormat(String s) {
        if (s == null || s.trim().isEmpty()) {
            return SchemaField.EditFormat.NONE;
        }
        try {
            return SchemaField.EditFormat.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SchemaField.EditFormat.NONE;
        }
    }

    private static SchemaField.Usage parseUsage(String s) {
        if (s == null || s.trim().isEmpty()) {
            return SchemaField.Usage.DISPLAY;
        }
        String u = s.trim().toUpperCase().replace("-", "");
        switch (u) {
            case "DISPLAY":
                return SchemaField.Usage.DISPLAY;
            case "COMP3", "PACKEDDECIMAL":
                return SchemaField.Usage.COMP3;
            case "COMP", "COMP4", "COMP5", "BINARY":
                return SchemaField.Usage.BINARY;
            case "COMP1", "COMPUTATIONAL1":
                return SchemaField.Usage.COMP1;
            case "COMP2", "COMPUTATIONAL2":
                return SchemaField.Usage.COMP2;
            default:
                throw new IllegalArgumentException("USAGE not supported: " + s);
        }
    }

    private static SchemaField.Kind inferKind(String javaType, SchemaField.Usage usage) {
        if (javaType == null) {
            return SchemaField.Kind.ALPHANUMERIC;
        }
        switch (javaType) {
            case "int", "long", "short", "BigDecimal", "float", "double", "Float", "Double":
                return SchemaField.Kind.NUM;
            case "NString":
                // PIC N national data — encoded via encodeDbcs so padding is the
                // fullwidth space (0x81 0x40) rather than the ASCII blank.
                return SchemaField.Kind.DBCS;
            case "String":
            default:
                return SchemaField.Kind.ALPHANUMERIC;
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
