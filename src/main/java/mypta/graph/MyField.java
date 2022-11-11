package mypta.graph;

import pascal.taie.language.classes.JField;

public class MyField extends Pointer{

    JField field;
    MemoryObj mem;
    public MyField(JField f) {
        this.field = f;
    }

    public void setMemoryObject(MemoryObj mem) {
        this.mem = mem;
    }
    @Override
    public String getRef() {
        if (field == null) return String.format("%s.field", this.mem);
        return field.toString();
    }
}
