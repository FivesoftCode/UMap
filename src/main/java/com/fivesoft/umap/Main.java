package com.fivesoft.umap;

import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.format.FormatWriter;
import com.fivesoft.umap.formats.BinaryFormat;
import com.fivesoft.umap.formats.JSONFormat;
import com.fivesoft.umap.formats.XMLFormat;
import com.fivesoft.umap.formats.YAMLFormat;
import com.fivesoft.umap.template.ArrayTemplate;
import com.fivesoft.umap.template.MapTemplate;

import java.io.ByteArrayOutputStream;

public class Main {

    public static void main(String[] args) {

        MapTemplate addressTemplate = new MapTemplate.Builder()
                .addOptional("street", String.class)
                .addRequired("city", String.class)
                .addRequired("zip", Integer.class)
                .build();

        MapTemplate gradeTemplate = new MapTemplate.Builder()
                .addRequired("grade", Integer.class)
                .addRequired("course", String.class)
                .addRequired("teacher", String.class)
                .addOptional("comment", String.class)
                .build();

        MapTemplate studentTemplate = new MapTemplate.Builder()
                .addRequired("name", String.class)
                .addRequired("age", Integer.class)
                .addRequired("gender", Boolean.class)
                .addRequired("address", addressTemplate)
                .addRequired("grades", gradeTemplate.asArray())
                .build();

        long start = System.nanoTime();

        UMap address = new UMap.Builder(addressTemplate)
                .set("street", "123 Main St.")
                .set("city", "New York")
                .set("zip", 12345)
                .build();

        UArray array = new UArray.Builder(gradeTemplate.asArray())
                .add(new UMap.Builder(gradeTemplate, true)
                        .set("grade", 90)
                        .set("course", "Math")
                        .set("teacher", "Mr. Smith")
                        .build())
                .add(new UMap.Builder(gradeTemplate, true)
                        .set("grade", 85)
                        .set("course", "Science")
                        .set("teacher", "Mrs. Johnson")
                        .set("comment", "Good job!")
                        .build())
                .build();

        UMap student = new UMap.Builder(studentTemplate, true)
                .set("name", "John")
                .set("age", "20")
                .set("gender", "true")
                .set("address", address)
                .set("grades", array)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            student.format(baos, new FormatWriter.Options(true, 2), new BinaryFormat(), 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to format UMap");
        }

        double end = System.nanoTime();

        byte[] bytes = baos.toByteArray();

        System.out.println("Formatted UMap");
        System.out.println(new String(bytes));
        System.out.println("Time: " + (end - start) / 1000000.0 + "ms");


        System.out.println("Parsing UMap");
        try {
            UMap studentMap = studentTemplate.readFormat(bytes, new BinaryFormat(), null);
            System.out.println(studentMap);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            studentMap.format(baos2, new FormatWriter.Options(true, 2), new YAMLFormat(), 1);
            System.out.println(baos2);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to format UMap");
        }

    }

}