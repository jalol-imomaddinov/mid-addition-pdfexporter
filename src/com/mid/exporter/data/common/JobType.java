package com.mid.exporter.data.common;

/**
 * @author
 */
public enum JobType {

    WELDER(0, "��������"),
    PAINTER(1, "�����"),
    OFFICE_WORKER(2, "�����");


    private final int id;
    private final String name;

    JobType(int id, String name) {
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
	return name;
    }

    public static JobType jobById(int id) {
	for (int i = 0; i < values().length; i++) {
	    JobType value = values()[i];
	    if (value.getId() == id) {
		return value;
	    }
	}

	return null;
    }
}
