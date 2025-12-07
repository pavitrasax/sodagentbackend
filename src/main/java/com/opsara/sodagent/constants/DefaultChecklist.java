package com.opsara.sodagent.constants;

public final class DefaultChecklist {

    public static final String ASSESSMENT_URL = "/fillsodchecklist?hashtoken=";
    public static final String DEFAULT_CHECKLIST_JSON = """
            {
              "title": "SOD Agent Template",
              "version": "1.0",
              "sections": [
                {
                  "id": "",
                  "title": "",
                  "questions": [
                    {
                      "id": "q1",
                      "type": "mcq-single",
                      "prompt": "Have doors been unlocked and the alarm system disabled?",
                      "options": ["Yes", "No", "Not Applicable"],
                      "required": true,
                      "attachment": {
                        "enabled": false,
                        "required": false
                      }
                    },
                    {
                      "id": "q2",
                      "type": "mcq-single",
                      "prompt": "Is the CCTV system functioning and recording?",
                      "options": ["Yes", "No", "Not Applicable"],
                      "required": true,
                      "attachment": {
                        "enabled": true,
                        "required": true
                      }
                    },
                    {
                      "id": "q3",
                      "type": "mcq-single",
                      "prompt": "Have all floors been swept and mopped?",
                      "options": ["Yes", "No", "Not Applicable"],
                      "required": true,
                      "attachment": {
                        "enabled": true,
                        "required": false
                      }
                    },
                    {
                      "id": "q4",
                      "type": "mcq-single",
                      "prompt": "Is background music playing at the correct volume?",
                      "options": ["Yes", "No", "Not Applicable"],
                      "required": true,
                      "attachment": {
                        "enabled": false,
                        "required": false
                      }
                    },
                    {
                      "id": "q5",
                      "type": "mcq-single",
                      "prompt": "Are all POS systems powered on and operational?",
                      "options": ["Yes", "No"],
                      "required": true,
                      "attachment": {
                        "enabled": false,
                        "required": false
                      }
                    },
                    {
                      "id": "q6",
                      "type": "mcq-single",
                      "prompt": "Are fitting rooms stocked with hangers, hooks, and clean seating?",
                      "options": ["Yes", "No", "Not Applicable"],
                      "required": true,
                      "attachment": {
                        "enabled": false,
                        "required": false
                      }
                    }
                  ]
                }
              ],
              "agentScope": "SOD",
              "description": "This is SOD Checklist Template"
            }
    """;
}
