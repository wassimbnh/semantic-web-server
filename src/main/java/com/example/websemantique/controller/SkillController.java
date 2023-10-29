package com.example.websemantique.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import tools.JenaEngine;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/skill")
public class SkillController {

    @GetMapping(value = "/getall", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllSkills() throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Skill ?Name\n" +
                "WHERE {\n" +
                "?Skill rdf:type ns:Skill ;\n" +
                "     ns:hasName ?Name .\n" +
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

    @GetMapping(value = "/getbyName/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSkillByName(@PathVariable(value = "name") String name) throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Skill ?Name\n" +
                "WHERE {\n" +
                "?Skill rdf:type ns:Skill ;\n" +
                "     ns:hasName ?Name .\n" +
                "FILTER (regex(?Name, '" + name + "', 'i'))\n" +  // Case-insensitive name filter
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

    @PostMapping(value = "/addSkill", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addSkill(@RequestBody String skillDetails) throws UnsupportedEncodingException {
        // Parse the incoming JSON data
        JSONObject skillJson = new JSONObject(skillDetails);

        // Extract skill details from the JSON
        String name = skillJson.getString("name");

        // Create a new model for the RDF data
        Model model = JenaEngine.readModel("data/company.owl");

        // Create an RDF resource for the new skill
        String skillUri = "http://www.example.com/ontologies/company#" + name.replaceAll(" ", "");
        Resource skillResource = model.createResource(skillUri);

        // Add properties to the skill resource
        skillResource.addProperty(model.createProperty("http://www.example.com/ontologies/company#hasName"), name)
                .addProperty(model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        model.createResource("http://www.example.com/ontologies/company#Skill"));

        // Save the updated model back to your RDF dataset
        JenaEngine.saveModel("data/company.owl", model);

        return "Skill added successfully.";
    }

    @GetMapping(value = "/getSkillByJobName/{jobName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSkillByJobName(@PathVariable(value = "jobName") String jobName) throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?Skill ?SkillName\n" +
                "WHERE {\n" +
                "?Job rdf:type ns:Job ;\n" +
                "     ns:hasName ?JobName .\n" +
                "?Skill rdf:type ns:Skill ;\n" +
                "     ns:hasJob ?Job ;\n" +
                "     ns:hasName ?SkillName .\n" +
                "FILTER (str(?JobName) = '" + jobName + "')\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        // Write the results to a JSON string
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

}
