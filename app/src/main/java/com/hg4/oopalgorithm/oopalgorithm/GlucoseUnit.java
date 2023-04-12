package com.hg4.oopalgorithm.oopalgorithm;

public enum GlucoseUnit {
    MGDL("mg/dl"){
        @Override
        public double convertFrom(double sourceValue, GlucoseUnit sourceUnit) {
            return (sourceUnit == MMOL) ? sourceValue * conversionValue : sourceValue;
        }
    },
    MMOL("mmol/L"){
        @Override
        public double convertFrom(double sourceValue, GlucoseUnit sourceUnit) {
            return (sourceUnit == MGDL) ? sourceValue / conversionValue : sourceValue;
        }
    };
    private static final double conversionValue = 18.018;
    private final String unitText;
    GlucoseUnit(String unitText){
        this.unitText = unitText;
    }
    public String getString() {
        return this.unitText;
    }
    public abstract double convertFrom(double sourceValue, GlucoseUnit sourceUnit);
}
