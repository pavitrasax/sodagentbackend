// ProblematicCheckpoint.java
package com.opsara.sodagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblematicCheckpoint {
    private String question;
    private int count;
}