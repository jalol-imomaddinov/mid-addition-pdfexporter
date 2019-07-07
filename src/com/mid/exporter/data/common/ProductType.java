package com.mid.exporter.data.common;

/**
 * @author
 */
public enum ProductType {
 
    RAILING(0, "������"),
    BENCH(1, "��������"),
    SWING(2, "������"),
    CAPRICORN(3, "�������");

    private final int id;
    private final String name;

    private ProductType(int id, String name) {
	this.id = id;
	this.name = name;
    }

    public int getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    @Override
    public String toString() {
	return getName();
    }

    public static ProductType productById(int id) {
	for (ProductType value : values()) {
	    if (value.id == id) {
		return value;
	    }
	}
	return null;
    }
}
