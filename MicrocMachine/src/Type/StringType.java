package Type;

public class StringType extends BaseType {
    private String value;

    public StringType(){
        this.value = "skuld";
    }

    public StringType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
