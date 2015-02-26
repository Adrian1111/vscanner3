package javaeetutorial.order.web;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.primefaces.model.StreamedContent;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javaeetutorial.order.ejb.RequestBean;
import javaeetutorial.order.entity.CustomerOrder;
import javaeetutorial.order.entity.LineItem;
import javaeetutorial.order.entity.Part;
import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIParameter;
import javax.faces.event.ActionEvent;


import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
 
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@ManagedBean
@SessionScoped
public class OrderManager implements Serializable {

    private static final long serialVersionUID = 2142383151318489373L;
    @EJB
    private RequestBean request;
    private static final Logger logger = Logger.getLogger("order.web.OrderManager");
    private List<CustomerOrder> orders;
    private Integer currentOrder;
    private Integer newOrderId;
    private String newOrderShippingInfo;
    private char newOrderStatus;
    private int newOrderDiscount;
    private List<Part> newOrderParts;
    private List<Part> newOrderSelectedParts;
    private String vendorName;
    private List<String> vendorSearchResults;
    private String selectedPartNumber;
    private int selectedPartRevision;
    private Long selectedVendorPartNumber;
    private Boolean findVendorTableDisabled = false;
    private Boolean partsTableDisabled = true;
    private String url;
    private UploadedFile file;

    public void handleFileUpload(FileUploadEvent event) {
        try{
                        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        catch (Exception e){
            logger.warning("Nie moge uploadowac pliku");
    }
         try {
            File targetFolder = new File("C:\\Users\\Administrator\\Downloads\\vscanner\\vs2\\target\\vs2-7.0.5\\resources\\images");
            InputStream inputStream = event.getFile().getInputstream();
            OutputStream out = new FileOutputStream(new File(targetFolder,
                    event.getFile().getFileName()));
            this.url = event.getFile().getFileName();
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            inputStream.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String partNumber;

    /**
     * @return the orders
     */
    public List<CustomerOrder> getOrders() {
        try {
            this.orders = request.getOrders();
        } catch (Exception e) {
            logger.warning("Couldn't get orders.");
        }
        return orders;
    }

    public List<LineItem> getLineItems() {
        try {
            return request.getLineItems(this.currentOrder);
        } catch (Exception e) {
            logger.warning("Couldn't get lineItems.");
            return null;
        }
    }

    public void removeOrder(ActionEvent event) {
        try {
            UIParameter param = (UIParameter) event.getComponent().findComponent("deleteOrderId");
            Integer id = Integer.parseInt(param.getValue().toString());
            request.removeOrder(id);
            logger.log(Level.INFO, "Removed order {0}.", id);
        } catch (NumberFormatException e) {
        }
    }

    
    //cos jeszcze jest nie tak jak powinno poprawic
    public void removeLineItem(ActionEvent event) {
        try {
            UIParameter param = (UIParameter) event.getComponent().findComponent("deleteLineItem");
            Integer id = Integer.parseInt(param.getValue().toString());
            request.removeLineItem(id);
            logger.log(Level.INFO, "Removed lineItem {0}.", id);

        } catch (NumberFormatException e) {

        }

    }

    public void findVendor() {
        try {
            this.findVendorTableDisabled = true;
            this.vendorSearchResults = (List<String>) request.locateVendorsByPartialName(vendorName);
            logger.log(Level.INFO, "Znaleziono {0} prawników używając stringa {1}.",
                    new Object[]{vendorSearchResults.size(), vendorName});
        } catch (Exception e) {
            logger.warning("Problem z wywołaniem RequestBean.locateVendorsByPartialName z findVendor");
        }
    }

    public void submitOrder() {
        try {
            request.createOrder(newOrderId, newOrderStatus, newOrderDiscount,
                    newOrderShippingInfo);

            logger.log(Level.INFO, "Created new order with order ID {0}, status {1}, "
                    + "discount {2}, and shipping info {3}.",
                    new Object[]{newOrderId, newOrderStatus, newOrderDiscount, newOrderShippingInfo});
            this.newOrderId = null;
            this.newOrderDiscount = 0;
            this.newOrderParts = null;
            this.newOrderShippingInfo = null;
        } catch (Exception e) {
            logger.warning("Problem creating order in submitOrder.");
        }
    }

    public void addPicture() {
        try {
            request.createPart(this.selectedPartNumber, 1, "ABC PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", "/resources/images/" + this.url);//this.file.getContents());//"/resources/images/" + this.url);
            request.createVendorPart(this.selectedPartNumber, this.selectedPartRevision,
"PART5", 345.67, 100);
        } catch (Exception e) {
            logger.warning("Problem ze stworzeniem propozycji skanu.");
        }
    }

    public void addLineItem() {
        try {
            List<LineItem> lineItems = request.getLineItems(currentOrder);
            logger.log(Level.INFO, "There are {0} line items in {1}.",
                    new Object[]{lineItems.size(), currentOrder});
            request.addLineItem(this.currentOrder,
                    this.selectedPartNumber,
                    this.selectedPartRevision,
                    1);
            logger.log(Level.INFO, "Adding line item to order # {0}",
                    this.currentOrder);
            //this.clearSelected();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem adding line items to order ID {0}",
                    newOrderId);
        }
    }

    /**
     * @param orders the orders to set
     */
    public void setOrders(List<CustomerOrder> orders) {
        this.orders = orders;
    }

    /**
     * @return the currentOrder
     */
    public int getCurrentOrder() {
        return currentOrder;
    }

    /**
     * @param currentOrder the currentOrder to set
     */
    public void setCurrentOrder(int currentOrder) {
        this.currentOrder = currentOrder;
    }

    /**
     * @return the newOrderId
     */
    public Integer getNewOrderId() {
        return newOrderId;
    }

    /**
     * @param newOrderId the newOrderId to set
     */
    public void setNewOrderId(Integer newOrderId) {
        this.newOrderId = newOrderId;
    }

    /**
     * @return the newOrderShippingInfo
     */
    public String getNewOrderShippingInfo() {
        return newOrderShippingInfo;
    }

    /**
     * @param newOrderShippingInfo the newOrderShippingInfo to set
     */
    public void setNewOrderShippingInfo(String newOrderShippingInfo) {
        this.newOrderShippingInfo = newOrderShippingInfo;
    }

    /**
     * @return the newOrderStatus
     */
    public char getNewOrderStatus() {
        return newOrderStatus;
    }

    /**
     * @param newOrderStatus the newOrderStatus to set
     */
    public void setNewOrderStatus(char newOrderStatus) {
        this.newOrderStatus = newOrderStatus;
    }

    /**
     * @return the newOrderDiscount
     */
    public int getNewOrderDiscount() {
        return newOrderDiscount;
    }

    /**
     * @param newOrderDiscount the newOrderDiscount to set
     */
    public void setNewOrderDiscount(int newOrderDiscount) {
        this.newOrderDiscount = newOrderDiscount;
    }

    /**
     * @return the newOrderParts
     */
    public List<Part> getNewOrderParts() {
        return request.getAllParts();
    }

    /**
     * @param newOrderParts the newOrderParts to set
     */
    public void setNewOrderParts(List<Part> newOrderParts) {
        this.newOrderParts = newOrderParts;
    }

    /**
     * @return the vendorName
     */
    public String getVendorName() {
        return vendorName;
    }

    /**
     * @param vendorName the vendorName to set
     */
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    /**
     * @return the vendorSearchResults
     */
    public List<String> getVendorSearchResults() {
        return vendorSearchResults;
    }

    /**
     * @param vendorSearchResults the vendorSearchResults to set
     */
    public void setVendorSearchResults(List<String> vendorSearchResults) {
        this.vendorSearchResults = vendorSearchResults;
    }

    /**
     * @return the newOrderSelectedParts
     */
    public List<Part> getNewOrderSelectedParts() {
        return newOrderSelectedParts;
    }

    /**
     * @param newOrderSelectedParts the newOrderSelectedParts to set
     */
    public void setNewOrderSelectedParts(List<Part> newOrderSelectedParts) {
        Iterator<Part> i = newOrderSelectedParts.iterator();
        while (i.hasNext()) {
            Part part = i.next();
            logger.log(Level.INFO, "Selected part {0}.", part.getPartNumber());
        }
        this.newOrderSelectedParts = newOrderSelectedParts;
    }

    /**
     * @return the selectedPartNumber
     */
    public String getSelectedPartNumber() {
        return selectedPartNumber;
    }

    /**
     * @param selectedPartNumber the selectedPartNumber to set
     */
    public void setSelectedPartNumber(String selectedPartNumber) {
        this.selectedPartNumber = selectedPartNumber;
    }

    /**
     * @return the selectedPartRevision
     */
    public int getSelectedPartRevision() {
        return selectedPartRevision;
    }

    /**
     * @param selectedPartRevision the selectedPartRevision to set
     */
    public void setSelectedPartRevision(int selectedPartRevision) {
        this.selectedPartRevision = selectedPartRevision;
    }

    /**
     * @return the selectedVendorPartNumber
     */
    public Long getSelectedVendorPartNumber() {
        return selectedVendorPartNumber;
    }

    /**
     * @param selectedVendorPartNumber the selectedVendorPartNumber to set
     */
    public void setSelectedVendorPartNumber(Long selectedVendorPartNumber) {
        this.selectedVendorPartNumber = selectedVendorPartNumber;
    }

    private void clearSelected() {
        this.setSelectedPartNumber(null);
        this.setSelectedPartRevision(0);
        this.setSelectedVendorPartNumber(new Long(0));
    }

    /**
     * @return the findVendorTableDisabled
     */
    public Boolean getFindVendorTableDisabled() {
        return findVendorTableDisabled;
    }

    /**
     * @param findVendorTableDisabled the findVendorTableDisabled to set
     */
    public void setFindVendorTableDisabled(Boolean findVendorTableDisabled) {
        this.findVendorTableDisabled = findVendorTableDisabled;
    }

    /**
     * @return the partsTableDisabled
     */
    public Boolean getPartsTableDisabled() {
        return partsTableDisabled;
    }

    /**
     * @param partsTableDisabled the partsTableDisabled to set
     */
    public void setPartsTableDisabled(Boolean partsTableDisabled) {
        this.partsTableDisabled = partsTableDisabled;
    }
}
