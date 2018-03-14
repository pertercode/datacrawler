package bean;

public class TypeName {

    private String _id;
    private String tName;

    private String categoryId;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }


    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public TypeName() {
    }

    public TypeName(String _id, String tName, String categoryId) {
        this._id = _id;
        this.tName = tName;
        this.categoryId = categoryId;
    }
}
