package dk.dma.embryo.user.model;

public class Feedback {
    private String name;
    private String emailAddress;
    private String mmsiNumber;
    private String message;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMmsiNumber() {
        return mmsiNumber;
    }

    public void setMmsiNumber(String mmsiNumber) {
        this.mmsiNumber = mmsiNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
