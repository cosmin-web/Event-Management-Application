package com.example.eventservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PackageEventDTO {
    @NotNull(message = "ID-ul pachetului este obligatoriu")
    private Integer packageId;

    @NotNull(message = "ID-ul evenimentului este obligatoriu")
    private Integer eventId;

    @Size(max = 100, message = "Numele pachetului prea lung")
    private String packageName;

    @Size(max = 100, message = "Numele evenimentului prea lung")
    private String eventName;

    @Size(max = 150, message = "Locatia prea lunga")
    private String eventLocation;

    @Size(max = 500, message = "Descrierea prea lunga")
    private String eventDescription;

    public PackageEventDTO() {}

    public PackageEventDTO(Integer packageId, Integer eventId) {
        this.packageId = packageId;
        this.eventId = eventId;
    }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
}