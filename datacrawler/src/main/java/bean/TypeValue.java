package bean;

/**
 * Created by asus on 2018/3/11.
 */
public class TypeValue {
    private String _id;
    private String tValue;

    private String categoryId;
    private String typeNameId;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String gettValue() {
        return tValue;
    }

    public void settValue(String tValue) {
        this.tValue = tValue;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTypeNameId() {
        return typeNameId;
    }

    public void setTypeNameId(String typeNameId) {
        this.typeNameId = typeNameId;
    }

    public TypeValue() {
    }

    public TypeValue(String _id, String tValue, String categoryId, String typeNameId) {
        this._id = _id;
        this.tValue = tValue;
        this.categoryId = categoryId;
        this.typeNameId = typeNameId;
    }
}
