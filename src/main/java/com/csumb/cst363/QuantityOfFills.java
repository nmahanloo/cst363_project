package com.csumb.cst363;

public class QuantityOfFills {
    private String pharmacyId;
    private String startDate;
    private String endDate;
    private String tradeName;
    private String quantity;

    private String no;

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Override
    public String toString() {
        return ("report [pharmacyId=" + pharmacyId + ", drug_name=" + tradeName + ", quantity="
                + quantity + ", start_date=" + startDate + ", end_date=" + endDate);
    }
}
