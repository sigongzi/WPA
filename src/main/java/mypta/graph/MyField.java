package mypta.graph;

import pascal.taie.language.classes.JField;

public class MyField extends Pointer{

    JField field;
    public MyField(JField f) {
        this.field = f;
    }

    @Override
    public String getRef() {
        if (field == null) return "null field";
        return field.toString();
    }
}
