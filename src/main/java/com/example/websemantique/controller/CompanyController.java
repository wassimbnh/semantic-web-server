package com.example.websemantique.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import tools.JenaEngine;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/company")
public class CompanyController {

    @GetMapping(value = "/getall", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllCompanies() throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Company ?Name ?Location ?Field\n" +
                "WHERE {\n" +
                "?Company ns:hasName ?Name ;\n" +
                "         ns:hasLocation ?Location ;\n" +
                "         ns:hasField ?Field ;\n" +
                "         rdf:type ns:Company .\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        // write to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        // and turn that into a String
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        System.out.println(j.getJSONObject("results").getJSONArray("bindings").isEmpty());

        Boolean isEmpty = j.getJSONObject("results").getJSONArray("bindings").isEmpty();

        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @GetMapping(value = "/getbyName/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCompanyByName(@PathVariable(value = "name") String name) throws UnsupportedEncodingException {
        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Company ?Name ?Location ?Field\n" +  // Include the "Field" attribute
                "WHERE {\n" +
                "?Company ns:hasName ?Name ;\n" +
                "         ns:hasLocation ?Location ;\n" +
                "         rdf:type ns:Company ;\n" +
                "         ns:hasField ?Field .\n" +  // Include the "hasField" property
                "FILTER (regex(?Name, '" + name + "', 'i'))\n" +  // Case-insensitive name filter
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        // write to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        // and turn that into a String
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        System.out.println(j.getJSONObject("results").getJSONArray("bindings").isEmpty());

        Boolean isEmpty = j.getJSONObject("results").getJSONArray("bindings").isEmpty();

        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @GetMapping(value = "/getAllEmployees", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllEmployees() throws UnsupportedEncodingException {
        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?Employee ?EmployeeName ?Company ?CompanyName ?Field\n" +
                "WHERE {\n" +
                "?Employee rdf:type ns:Employee ;\n" +
                "         ns:hasName ?EmployeeName ;\n" +
                "         ns:worksFor ?Company .\n" +
                "?Company rdf:type ns:Company ;\n" +
                "         ns:hasName ?CompanyName ;\n" +
                "         ns:hasField ?Field .\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");
        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @GetMapping(value = "/getEmployeesByCompanyName/{companyName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEmployeesByCompanyName(@PathVariable(value = "companyName") String companyName) throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?Employee ?EmployeeName\n" +
                "WHERE {\n" +
                "?Company ns:hasName ?CompanyName ;\n" +
                "         ns:hasEmployee ?Employee .\n" +
                "?Employee ns:hasName ?EmployeeName .\n" +
                "FILTER (str(?CompanyName) = '" + companyName + "')\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        // write the results to a JSON string
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        Boolean isEmpty = j.getJSONObject("results").getJSONArray("bindings").isEmpty();

        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @PostMapping(value = "/addCompany", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addCompany(@RequestBody String companyDetails) throws UnsupportedEncodingException {
        // Parse the incoming JSON data
        JSONObject companyJson = new JSONObject(companyDetails);

        // Extract company details from the JSON
        String name = companyJson.getString("name");
        String location = companyJson.getString("location");
        String field = companyJson.getString("field");

        // Create a new model for the RDF data
        Model model = JenaEngine.readModel("data/company.owl");

        // Create an RDF resource for the new company
        String companyUri = "http://www.example.com/ontologies/company#" + name.replaceAll(" ", "");
        Resource companyResource = model.createResource(companyUri);

        // Add properties to the company resource
        companyResource.addProperty(model.createProperty("http://www.example.com/ontologies/company#hasName"), name)
                .addProperty(model.createProperty("http://www.example.com/ontologies/company#hasLocation"), location)
                .addProperty(model.createProperty("http://www.example.com/ontologies/company#hasField"), field)
                .addProperty(model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        model.createResource("http://www.example.com/ontologies/company#Company"));

        // Save the updated model back to your RDF dataset
        JenaEngine.saveModel("data/company.owl", model);

        return "Company added successfully.";
    }

}
