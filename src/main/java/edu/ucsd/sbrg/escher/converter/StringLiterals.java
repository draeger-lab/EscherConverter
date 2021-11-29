package edu.ucsd.sbrg.escher.converter;

public interface StringLiterals {


    interface BiggCompartmentStrings {

        String cytosol = "c";
        String extracellularSpace = "e";
        String periplasm = "p";
        String mitochondria = "m";
        String peroxisome_glyoxysome = "x";
        String endoplasmicReticulum = "r";
        String vacuole = "v";
        String nucleus = "n";
        String golgiApparatus = "g";
        String thylakoid = "u";
        String lysosome = "l";
        String chloroplast = "h";
        String flagellum = "f";
        String eyespot = "s";
        String intermembraneSpaceMitochondria = "im";
        String carboxysome = "cx";
        String thylakoidMembrane = "um";
        String cytosolicMembrane = "cm";
        String innerMitochondrialCompartment = "i";
        String mitochondrialIntermembrane = "mm";
        String wildtypeStaphAureus = "w";
        String cytochromeComplex = "y";

    }

    // Affixes for both
    String exchangeSuffix = "_EX";
    String metabolitePrefix = "M_";
    String reactionPrefix = "R_";

    // Affixes for Escher2SBML
    String speciesGlyphPrefix = "SG_";
    String speciesReferenceGlyphInfix = "_SRG_";
    String textGlyphPrefix = "TG_";
    String reactantInfix = "_REACTANT_";
    String productInfix = "_PRODUCT_";
    String modifierInfix = "_MODIFIER_";
    String compartmentGlyphSuffix = "_GLYPH";

    // Affixes for Escher2SBGN
    String cardinalityGlyphPrefix = "CARDINALITY_";
    String sinkSuffix = "_SINK";
    String sourceSuffix = "_SOURCE";


}
