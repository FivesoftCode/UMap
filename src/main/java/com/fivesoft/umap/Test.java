package com.fivesoft.umap;

public class Test {

    public static final String TEST_JSON = """
            {
              "age": 20,
              "gender": true,
              "name": "John",
              "address": {
                "city": "New York",
                "street": null,"zip": 12345
                }
              }
            """;

    public static final String TEST_JSON_COMPLEX = """
            {
              "age": 20,
              "gender": true,
              "name": "John",
              "address": {
                "city": "New York",
                "street": "123 Main St.",
                "zip": 12345
              },
              "grades": [
                {
                  "comment": null,
                  "course": "Math",
                  "grade": 90,
                  "teacher": "Mr. Smith"
                },
                {
                  "comment": "Good job!",
                  "course": "Science",
                  "grade": 85,
                  "teacher": "Mrs. Johnson"
                }
              ]
            }
              """;

    public static final String TEST_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
              <age>20</age>
              <gender>true</gender>
              <name>John</name>
              <address>
                <city>New York</city>
                <street>123 Main St.</street>
                <zip>12345</zip>
              </address>
            </root>
            """;

    public static final String TEST_XML_COMPLEX = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
              <age>20</age>
              <gender>true</gender>
              <name>John</name>
              <address>
                <city>New York</city>
                <street>123 Main St.</street>
                <zip>12345</zip>
              </address>
              <grades>
                <item n="0">
                  <course>Math</course>
                  <grade>90</grade>
                  <teacher>Mr. Smith</teacher>
                </item>
                <item n="1">
                  <comment>Good job!</comment>
                  <course>Science</course>
                  <grade>85</grade>
                  <teacher>Mrs. Johnson</teacher>
                </item>
              </grades>
            </root>
            """;

}
