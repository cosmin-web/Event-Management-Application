package com.example.clientservice.infrastructure.adapter.out.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketData {

    private String cod;

    private EventShort event;

    @JsonProperty("package")
    private PackageShort _package;

    public TicketData() {}


    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public EventShort getEvent() { return event; }
    public void setEvent(EventShort event) { this.event = event; }

    public PackageShort get_package() { return _package; }
    public void set_package(PackageShort _package) { this._package = _package; }
}
