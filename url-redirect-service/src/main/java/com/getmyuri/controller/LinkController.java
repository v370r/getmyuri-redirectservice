package com.getmyuri.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.getmyuri.dto.LinkDTO;
import com.getmyuri.model.ClickMetric;
import com.getmyuri.model.DataObjectFormat;
import com.getmyuri.repository.ClickMetricRepository;
import com.getmyuri.service.LinkService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/links")
public class LinkController {

    private static final Logger logger = LoggerFactory.getLogger(LinkController.class);

    @Autowired
    private LinkService linkService;

    @Autowired
    private ClickMetricRepository clickMetricRepository;

    @PostMapping
    public ResponseEntity<DataObjectFormat> createLink(@RequestBody LinkDTO linkDTO) {
        logger.info("Received request to create link: alias={}, username={}", linkDTO.getAlias(),
                linkDTO.getUsername());
        DataObjectFormat savedLink = linkService.createLink(linkDTO);
        logger.info("Successfully created link with alias: {}", savedLink.getAlias());
        return ResponseEntity.ok(savedLink);
    }

    @GetMapping
    public ResponseEntity<List<DataObjectFormat>> getAllLinks() {
        logger.info("Fetching all links...");
        List<DataObjectFormat> links = linkService.getAllLinks();
        logger.info("Fetched {} links", links.size());
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/{id}") // Decomissioned
    public ResponseEntity<String> deleteLink(@PathVariable String id) {
        logger.info("Request to delete link by ID: {}", id);
        boolean deleted = linkService.deleteLink(id);
        if (deleted) {
            logger.info("Successfully deleted link with ID: {}", id);
            return ResponseEntity.ok("Deleted link with ID: " + id);
        } else {
            logger.warn("Link with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/**")
    public ResponseEntity<String> deleteLinkByAlias(HttpServletRequest request) {
        String aliasPath = request.getRequestURI().replaceFirst("/api/links/", "");
        logger.info("Request to delete link by alias path: {}", aliasPath);
        boolean deleted = linkService.deleteLinkByAliasPath(aliasPath);
        if (deleted) {
            logger.info("Successfully deleted alias path: {}", aliasPath);
            return ResponseEntity.ok("Deleted alias: " + aliasPath);
        } else {
            logger.warn("Alias path not found: {}", aliasPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" Alias not found: " + aliasPath);
        }
    }

    @GetMapping("/click-stats")
    public ResponseEntity<List<ClickMetric>> getClickStats(@RequestParam String username) {
        logger.info("Fetching click stats for user: {}", username);
        List<ClickMetric> stats = clickMetricRepository.findByUsernameOrderByClickDateDesc(username);
        logger.info("Found {} click metrics for user {}", stats.size(), username);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> aliasExists(@RequestParam String aliasPath) {
        logger.info("Checking if alias exists: {}", aliasPath);
        boolean exists = linkService.aliasExists(aliasPath);
        return ResponseEntity.ok(exists);
    }

}
