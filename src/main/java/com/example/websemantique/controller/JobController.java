package com.example.websemantique.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import tools.JenaEngine;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/job")
public class JobController {

    @GetMapping(value = "/getall", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllJobs() throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Job ?Name\n" +
                "WHERE {\n" +
                "?Job rdf:type ns:Job ;\n" +
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
    public String getJobByName(@PathVariable(value = "name") String name) throws UnsupportedEncodingException {

        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?Job ?Name\n" +
                "WHERE {\n" +
                "?Job rdf:type ns:Job ;\n" +
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

    @PostMapping(value = "/addJob", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addJob(@RequestBody String jobDetails) throws UnsupportedEncodingException {
        // Parse the incoming JSON data
        JSONObject jobJson = new JSONObject(jobDetails);

        // Extract job details from the JSON
        String name = jobJson.getString("name");

        // Create a new model for the RDF data
        Model model = JenaEngine.readModel("data/company.owl");

        // Create an RDF resource for the new job
        String jobUri = "http://www.example.com/ontologies/company#" + name.replaceAll(" ", "");
        Resource jobResource = model.createResource(jobUri);

        // Add properties to the job resource
        jobResource.addProperty(model.createProperty("http://www.example.com/ontologies/company#hasName"), name)
                .addProperty(model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        model.createResource("http://www.example.com/ontologies/company#Job"));

        // Save the updated model back to your RDF dataset
        JenaEngine.saveModel("data/company.owl", model);

        return "Job added successfully.";
    }
    @PostMapping(value = "/addJobWithSkills", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String addJobWithSkills(@RequestBody String jobDetails) throws UnsupportedEncodingException {
        // Parse the incoming JSON data
        JSONObject jobJson = new JSONObject(jobDetails);

        // Extract job details from the JSON
        String jobName = jobJson.getString("jobName");

        // Extract the skills related to the job from the JSON
        JSONArray skillsArray = jobJson.getJSONArray("skills");

        // Create a new model for the RDF data
        Model model = JenaEngine.readModel("data/company.owl");

        // Create an RDF resource for the new job
        String jobUri = "http://www.example.com/ontologies/company#" + jobName.replaceAll(" ", "");
        Resource jobResource = model.createResource(jobUri);

        // Add properties to the job resource
        jobResource.addProperty(model.createProperty("http://www.example.com/ontologies/company#hasName"), jobName)
                .addProperty(model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        model.createResource("http://www.example.com/ontologies/company#Job"));

        // Add skills related to the job
        for (int i = 0; i < skillsArray.length(); i++) {
            String skillName = skillsArray.getString(i);

            // Create an RDF resource for the skill
            String skillUri = "http://www.example.com/ontologies/company#" + skillName.replaceAll(" ", "");
            Resource skillResource = model.createResource(skillUri);

            // Link the skill to the job using a property, for example "requiresSkill"
            jobResource.addProperty(model.createProperty("http://www.example.com/ontologies/company#requiresSkill"), skillResource);
        }

        // Save the updated model back to your RDF dataset
        JenaEngine.saveModel("data/company.owl", model);

        return "Job with related skills added successfully.";
    }
    public class JobWithSkills {
        private String jobName;
        private List<String> skills;

        public JobWithSkills() {
            // Default constructor
        }

        public JobWithSkills(String jobName, List<String> skills) {
            this.jobName = jobName;
            this.skills = skills;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }
    }
    @GetMapping(value = "/getAllJobsWithSkills", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JobWithSkills> getAllJobsWithSkills() throws UnsupportedEncodingException {
        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT ?JobName ?SkillName\n" +
                "WHERE {\n" +
                "?Job rdf:type ns:Job ;\n" +
                "     ns:hasName ?JobName ;\n" +
                "     ns:requiresSkill ?Skill .\n" +
                "?Skill ns:hasName ?SkillName .\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        List<JobWithSkills> jobWithSkillsList = new ArrayList<>();

        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();
            String jobName = solution.getLiteral("JobName").getString();
            String skillName = solution.getLiteral("SkillName").getString();

            // Check if the job already exists in the list
            Optional<JobWithSkills> existingJob = jobWithSkillsList.stream()
                    .filter(j -> j.getJobName().equals(jobName))
                    .findFirst();

            if (existingJob.isPresent()) {
                existingJob.get().getSkills().add(skillName);
            } else {
                JobWithSkills newJob = new JobWithSkills();
                newJob.setJobName(jobName);
                List<String> skills = new ArrayList<>();
                skills.add(skillName);
                newJob.setSkills(skills);
                jobWithSkillsList.add(newJob);
            }
        }

        return jobWithSkillsList;
    }

    @GetMapping(value = "/getJobsWithSkills", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JobWithSkills> getJobsWithSkills() throws UnsupportedEncodingException {
        String qexec = "PREFIX ns: <http://www.example.com/ontologies/company#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?Job ?JobName ?SkillName\n" +
                "WHERE {\n" +
                "?Job rdf:type ns:Job ;\n" +
                "     ns:hasName ?JobName ;\n" +
                "     ns:hasSkill ?Skill .\n" +
                "?Skill ns:hasName ?SkillName .\n" +
                "}\n";

        Model model = JenaEngine.readModel("data/company.owl");

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();

        List<JobWithSkills> jobWithSkillsList = new ArrayList<>();

        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();
            String jobName = solution.getLiteral("JobName").getString();
            String skillName = solution.getLiteral("SkillName").getString();

            JobWithSkills jobWithSkills = jobWithSkillsList.stream()
                    .filter(jws -> jws.getJobName().equals(jobName))
                    .findFirst()
                    .orElseGet(() -> {
                        JobWithSkills jws = new JobWithSkills();
                        jws.setJobName(jobName);
                        jws.setSkills(new ArrayList<>());
                        jobWithSkillsList.add(jws);
                        return jws;
                    });

            jobWithSkills.getSkills().add(skillName);
        }

        return jobWithSkillsList;
    }



}
