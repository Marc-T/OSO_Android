package com.blackorwhite.osobackend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Marc on 02/12/2016.
 */

/**
 * The Objectify object model for device registrations we are persisting
 */
@Entity
public class ConfigRecord {

    @Id
    Long id;

    @Index
    String Key;

    Integer Value;
    // you can add more fields...

    public ConfigRecord(String Key) {
        this.Key = Key;
    }

    public ConfigRecord() {
    }

    public Integer GetValue() {
        return this.Value;
    }

    public void SetValue(Integer Value)
    {
        this.Value = Value;
    }
}