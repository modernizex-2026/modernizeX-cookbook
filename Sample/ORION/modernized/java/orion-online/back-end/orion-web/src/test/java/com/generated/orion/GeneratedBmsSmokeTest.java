package com.generated.orion;

import static org.junit.jupiter.api.Assertions.*;

import com.appruntime.FieldMapping;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Auto-generated smoke test — drift-guard giữa BmsMetadata (FieldMapping) và layout WORKING-STORAGE
 * XML. MapBinder chỉ bind field resolve được trong FieldStore (bind-miss là IM LẶNG lúc runtime) —
 * test này biến drift đó thành FAIL lúc build. Do not edit.
 */
public class GeneratedBmsSmokeTest {

    private static final String[] METADATA_CLASSES = {
        "com.generated.orion.ocaccta.metadata.OcacctaBmsMetadata",
        "com.generated.orion.ocacctl.metadata.OcacctlBmsMetadata",
        "com.generated.orion.ocacctu.metadata.OcacctuBmsMetadata",
        "com.generated.orion.ocacctv.metadata.OcacctvBmsMetadata",
        "com.generated.orion.ocactin.metadata.OcactinBmsMetadata",
        "com.generated.orion.ocadmen.metadata.OcadmenBmsMetadata",
        "com.generated.orion.ocanlin.metadata.OcanlinBmsMetadata",
        "com.generated.orion.ocauthq.metadata.OcauthqBmsMetadata",
        "com.generated.orion.ocbillp.metadata.OcbillpBmsMetadata",
        "com.generated.orion.occarda.metadata.OccardaBmsMetadata",
        "com.generated.orion.occardl.metadata.OccardlBmsMetadata",
        "com.generated.orion.occardu.metadata.OccarduBmsMetadata",
        "com.generated.orion.occardv.metadata.OccardvBmsMetadata",
        "com.generated.orion.occrdin.metadata.OccrdinBmsMetadata",
        "com.generated.orion.occusin.metadata.OccusinBmsMetadata",
        "com.generated.orion.occusta.metadata.OccustaBmsMetadata",
        "com.generated.orion.occustl.metadata.OccustlBmsMetadata",
        "com.generated.orion.occustu.metadata.OccustuBmsMetadata",
        "com.generated.orion.occustv.metadata.OccustvBmsMetadata",
        "com.generated.orion.ocdgrp.metadata.OcdgrpBmsMetadata",
        "com.generated.orion.ocmenu.metadata.OcmenuBmsMetadata",
        "com.generated.orion.ocops.metadata.OcopsBmsMetadata",
        "com.generated.orion.ocpauin.metadata.OcpauinBmsMetadata",
        "com.generated.orion.ocpwdch.metadata.OcpwdchBmsMetadata",
        "com.generated.orion.ocrept.metadata.OcreptBmsMetadata",
        "com.generated.orion.ocrptmn.metadata.OcrptmnBmsMetadata",
        "com.generated.orion.ocsgnon.metadata.OcsgnonBmsMetadata",
        "com.generated.orion.ocstmin.metadata.OcstminBmsMetadata",
        "com.generated.orion.ocstmv.metadata.OcstmvBmsMetadata",
        "com.generated.orion.octcat.metadata.OctcatBmsMetadata",
        "com.generated.orion.octrana.metadata.OctranaBmsMetadata",
        "com.generated.orion.octranl.metadata.OctranlBmsMetadata",
        "com.generated.orion.octranv.metadata.OctranvBmsMetadata",
        "com.generated.orion.octrnin.metadata.OctrninBmsMetadata",
        "com.generated.orion.octsrch.metadata.OctsrchBmsMetadata",
        "com.generated.orion.octtyp.metadata.OcttypBmsMetadata",
        "com.generated.orion.ocusra.metadata.OcusraBmsMetadata",
        "com.generated.orion.ocusrd.metadata.OcusrdBmsMetadata",
        "com.generated.orion.ocusrl.metadata.OcusrlBmsMetadata",
        "com.generated.orion.ocusru.metadata.OcusruBmsMetadata",
        "com.generated.orion.ocutil.metadata.OcutilBmsMetadata",
        "com.generated.orion.odacctu.metadata.OdacctuBmsMetadata",
        "com.generated.orion.odacctv.metadata.OdacctvBmsMetadata",
        "com.generated.orion.odcardv.metadata.OdcardvBmsMetadata",
        "com.generated.orion.odcustv.metadata.OdcustvBmsMetadata",
        "com.generated.orion.odtrana.metadata.OdtranaBmsMetadata",
        "com.generated.orion.odtranl.metadata.OdtranlBmsMetadata",
        "com.generated.orion.ouabnd.metadata.OuabndBmsMetadata",
        "com.generated.orion.ouactin.metadata.OuactinBmsMetadata",
        "com.generated.orion.ouanlin.metadata.OuanlinBmsMetadata",
        "com.generated.orion.ouarch.metadata.OuarchBmsMetadata",
        "com.generated.orion.oubkp.metadata.OubkpBmsMetadata",
        "com.generated.orion.ouchgf.metadata.OuchgfBmsMetadata",
        "com.generated.orion.ouclos.metadata.OuclosBmsMetadata",
        "com.generated.orion.oucrdin.metadata.OucrdinBmsMetadata",
        "com.generated.orion.oucusin.metadata.OucusinBmsMetadata",
        "com.generated.orion.oucycl.metadata.OucyclBmsMetadata",
        "com.generated.orion.oudate.metadata.OudateBmsMetadata",
        "com.generated.orion.oufee.metadata.OufeeBmsMetadata",
        "com.generated.orion.ouflag.metadata.OuflagBmsMetadata",
        "com.generated.orion.ouimp.metadata.OuimpBmsMetadata",
        "com.generated.orion.ouimspa.metadata.OuimspaBmsMetadata",
        "com.generated.orion.ouint.metadata.OuintBmsMetadata",
        "com.generated.orion.oumqreq.metadata.OumqreqBmsMetadata",
        "com.generated.orion.oupay.metadata.OupayBmsMetadata",
        "com.generated.orion.oupost.metadata.OupostBmsMetadata",
        "com.generated.orion.oupurg.metadata.OupurgBmsMetadata",
        "com.generated.orion.ournew.metadata.OurnewBmsMetadata",
        "com.generated.orion.oustmb.metadata.OustmbBmsMetadata",
        "com.generated.orion.oustmin.metadata.OustminBmsMetadata",
        "com.generated.orion.outrnin.metadata.OutrninBmsMetadata",
        "com.generated.orion.ouxref.metadata.OuxrefBmsMetadata"
    };

    @Test
    public void fieldMappingsResolveInLayout() throws Exception {
        for (String fqcn : METADATA_CLASSES) {
            Class<?> meta = Class.forName(fqcn);
            @SuppressWarnings("unchecked")
            List<String> maps = (List<String>) meta.getMethod("getMapNames").invoke(null);
            String layoutRes = (String) meta.getMethod("getLayoutResource").invoke(null);
            Set<String> layoutNames = layoutFieldNames(layoutRes);
            assertFalse(layoutNames.isEmpty(), fqcn + ": layout rỗng: " + layoutRes);
            for (String mapName : maps) {
                FieldMapping fm =
                        (FieldMapping)
                                meta.getMethod("getFieldMapping", String.class)
                                        .invoke(null, mapName);
                int checked = 0;
                for (Map<String, String> m :
                        List.of(
                                fm.getDataInFields(),
                                fm.getDataOutFields(),
                                fm.getAttrFields(),
                                fm.getLengthFields())) {
                    for (Map.Entry<String, String> e : m.entrySet()) {
                        assertTrue(
                                layoutNames.contains(e.getValue().toUpperCase()),
                                fqcn
                                        + " map "
                                        + mapName
                                        + ": COBOL field '"
                                        + e.getValue()
                                        + "' (BMS "
                                        + e.getKey()
                                        + ") không có trong "
                                        + layoutRes
                                        + " — drift metadata↔layout, MapBinder sẽ bind-miss im"
                                        + " lặng");
                        checked++;
                    }
                }
                assertTrue(checked > 0, fqcn + " map " + mapName + ": FieldMapping rỗng");
            }
        }
    }

    /** Gom mọi thuộc tính name trong layout XML (field + group), uppercase. */
    private static Set<String> layoutFieldNames(String resource) throws Exception {
        Set<String> names = new HashSet<>();
        try (java.io.InputStream is =
                GeneratedBmsSmokeTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(is, "layout resource không có trên classpath: " + resource);
            javax.xml.parsers.DocumentBuilderFactory f =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // XXE-safe
            org.w3c.dom.Document doc = f.newDocumentBuilder().parse(is);
            org.w3c.dom.NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                String n = ((org.w3c.dom.Element) all.item(i)).getAttribute("name");
                if (!n.isEmpty()) {
                    names.add(n.toUpperCase());
                }
            }
        }
        return names;
    }
}
