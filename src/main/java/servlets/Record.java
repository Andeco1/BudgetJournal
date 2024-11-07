package servlets;

public class Record {
    private String category_name,operation_date;
    private float total;

    public Record() {};

    public Record(float total, String operation_date, String category_name) {
        this.total = total;
        this.operation_date = operation_date;
        this.category_name = category_name;
    }

    public Record(boolean operation, String category_name, String operation_date, float total){
        this.total = total * (operation?-1:1);
        this.category_name = category_name;
        this.operation_date = operation_date;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getOperation_date() {
        return operation_date;
    }

    public void setOperation_date(String operation_date) {
        this.operation_date = operation_date;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Record{" +
                "category_name='" + category_name + '\'' +
                ", operation_date='" + operation_date + '\'' +
                ", total=" + total +
                '}';
    }
}
