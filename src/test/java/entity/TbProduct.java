package entity;


import java.sql.Date;

public class TbProduct {

  private int id;
  private String productName;
  private java.util.Date productTime;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }


  public java.util.Date getProductTime() {
    return productTime;
  }

  public void setProductTime(Date productTime) {
    this.productTime = productTime;
  }

}
