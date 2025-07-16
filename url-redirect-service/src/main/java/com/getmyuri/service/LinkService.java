package com.getmyuri.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.getmyuri.dto.LinkDTO;
import com.getmyuri.dto.ResolvedLinkDTO;
import com.getmyuri.model.DataObjectFormat;
import com.getmyuri.repository.LinkRepository;
import com.getmyuri.util.DateCalculations;

@Service
public class LinkService {

    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);

    @Autowired
    private LinkRepository linkRepository;

    @Value("${shortlink.default.ttl}")
    private String defaultTtlString;

    public DataObjectFormat createLink(LinkDTO linkDTO) {

        logger.info("Creating link for alias: {}", linkDTO.getAlias());
        String[] aliasParts = linkDTO.getAlias().split("/");

        Date startTime = linkDTO.getStartTime() != null ? linkDTO.getStartTime() : Date.from(Instant.now());
        String futureExpiry = linkDTO.getExpiresAt() != null ? linkDTO.getExpiresAt() : defaultTtlString;
        Date expiresAt = DateCalculations.calculateExpiryFrom(startTime, futureExpiry);
        if (aliasParts.length == 1) {
            // Check if a root link with the same alias already exists.
            Optional<DataObjectFormat> existingLinkOpt = linkRepository.findByAlias(aliasParts[0]);
            DataObjectFormat root;
            if (existingLinkOpt.isPresent()) {
                // If exists, update the record with new values.
                root = existingLinkOpt.get();
                root.setLink(linkDTO.getLink());
                root.setPassword(linkDTO.getPassword());
                root.setUsername(linkDTO.getUsername());
                root.setLocation(linkDTO.getLocation());
                root.setRadius(linkDTO.getRadius());
                root.setStartTime(startTime);
                root.setExpiresAt(expiresAt);
                logger.info("Updating existing root link: {}", root.getAlias());
            } else {
                root = DataObjectFormat.builder().alias(aliasParts[0])
                        .link(linkDTO.getLink())
                        .password(linkDTO.getPassword())
                        .username(linkDTO.getUsername())
                        .location(linkDTO.getLocation())
                        .radius(linkDTO.getRadius())
                        .startTime(startTime)
                        .expiresAt(expiresAt)
                        .build();
                logger.info("Saving new root link: {}", root.getAlias());
            }
            return linkRepository.save(root);
        }

        String rootAlias = aliasParts[0];
        Optional<DataObjectFormat> existingRoot = linkRepository.findByAlias(rootAlias);
        DataObjectFormat root = existingRoot.orElse(null);

        if ((root == null) && aliasParts.length == 1) {
            root = DataObjectFormat.builder().alias(aliasParts[0]).link(linkDTO.getLink())
                    .password(linkDTO.getPassword()).username(linkDTO.getUsername()).radius(linkDTO.getRadius())
                    .build();
        } else if (root == null) {
            root = DataObjectFormat.builder().alias(aliasParts[0]).username(linkDTO.getUsername()).build();
        }

        List<DataObjectFormat> currentLevel = root.getSublinks();
        DataObjectFormat current = null;

        for (int i = 1; i < aliasParts.length; i++) {
            String currentAlias = aliasParts[i];

            if (currentLevel == null) {
                currentLevel = new ArrayList<>();
                if (i == 1) {
                    root.setSublinks(currentLevel);
                } else if (current != null) {
                    current.setSublinks(currentLevel);
                }
            }

            Optional<DataObjectFormat> existing = currentLevel.stream()
                    .filter(sublink -> sublink.getAlias().equals(currentAlias))
                    .findFirst();

            if (existing.isPresent()) {
                current = existing.get();
            } else {
                current = DataObjectFormat.builder().alias(currentAlias).build();
                currentLevel.add(current);
            }

            if (i == aliasParts.length - 1) {
                current.setLink(linkDTO.getLink());
                current.setPassword(linkDTO.getPassword());
                current.setClicks(0);
                current.setLocation(linkDTO.getLocation());
                current.setRadius(linkDTO.getRadius());
                current.setStartTime(startTime);
                current.setExpiresAt(expiresAt);
                logger.info("Added/Updated sublink: {}", currentAlias);
            }

            currentLevel = current.getSublinks();
        }
        logger.info("Saving updated link hierarchy for root: {}", root.getAlias());
        return linkRepository.save(root);
    }

    public Optional<ResolvedLinkDTO> getLink(String aliasPath) {

        logger.info("Resolving alias path: {}", aliasPath);
        String[] parts = aliasPath.split("/");
        if (parts.length == 0)
            return Optional.empty();

        Optional<DataObjectFormat> rootOpt = linkRepository.findByAlias(parts[0]);
        if (rootOpt.isEmpty()) {
            logger.warn("Root alias not found: {}", parts[0]);
            return Optional.empty();
        }

        DataObjectFormat root = rootOpt.get();
        Date now = new Date();

        if (root.getStartTime() != null && now.before(root.getStartTime())) {
            logger.info("Root link not active yet: starts at {}", root.getStartTime());
            return Optional.empty();
        }

        if (root.getExpiresAt() != null && now.after(root.getExpiresAt())) {
            logger.info("Root link expired at {}", root.getExpiresAt());
            return Optional.empty();
        }

        if (rootOpt.get().getSublinks() == null) {
            logger.debug("No sublinks found for root alias: {}", parts[0]);
            return Optional.of(ResolvedLinkDTO.builder()
                    .alias(aliasPath)
                    .link(rootOpt.get().getLink())
                    .password(rootOpt.get().getPassword())
                    .username(rootOpt.get().getUsername())
                    .location(rootOpt.get().getLocation())
                    .radius(rootOpt.get().getRadius())
                    .build());

        }

        DataObjectFormat current = traverseSublinks(rootOpt.get().getSublinks(),
                Arrays.copyOfRange(parts, 1, parts.length));

        if (current == null) {
            logger.warn("Sublink not found for alias path: {}", aliasPath);
            return Optional.empty();
        }

        if (current.getStartTime() != null && now.before(current.getStartTime())) {
            logger.info("Current link not active yet: starts at {}", current.getStartTime());
            return Optional.empty();
        }

        if (current.getExpiresAt() != null && now.after(current.getExpiresAt())) {
            logger.info("Current link expired at {}", current.getExpiresAt());
            return Optional.empty();
        }

        if (parts.length == 1)
            return Optional.of(ResolvedLinkDTO.builder().alias(aliasPath).link(rootOpt.get().getLink()).build());

        logger.info("Successfully resolved alias path: {}", aliasPath);
        if ((current != null)
                && (current.getUsername() == null)) {
            return Optional.of(
                    ResolvedLinkDTO.builder()
                            .alias(aliasPath)
                            .link(current.getLink())
                            .password(current.getPassword())
                            .username(rootOpt.get().getUsername())
                            .location(current.getLocation())
                            .radius(current.getRadius())
                            .build());
        }

        return Optional.empty();
    }

    private DataObjectFormat traverseSublinks(List<DataObjectFormat> list, String[] path) {
        if (path.length == 0)
            return null;
        DataObjectFormat current = null;
        for (String part : path) {
            if (list == null)
                return null;
            current = list.stream().filter(l -> l.getAlias().equals(part)).findFirst().orElse(null);
            if (current == null)
                return null;
            list = current.getSublinks();
        }
        return current;
    }

    public List<DataObjectFormat> getAllLinks() {
        logger.info("Fetching all links from MongoDB...");
        return linkRepository.findAll();
    }

    public Optional<DataObjectFormat> getLinkByAlias(String alias) {
        logger.info("Fetching link by alias: {}", alias);
        return linkRepository.findByAlias(alias);
    }

    public boolean deleteLink(String id) {
        logger.info("Deleting link by ID: {}", id);
        if (linkRepository.existsById(id)) {
            linkRepository.deleteById(id);
            logger.info("Successfully deleted link with ID: {}", id);
            return true;
        }
        logger.warn("Attempted to delete non-existent link with ID: {}", id);
        return false;
    }

    public boolean deleteLinkByAliasPath(String aliasPath) {

        logger.info("Deleting link by alias path: {}", aliasPath);
        String[] parts = aliasPath.split("/");
        if (parts.length == 0)
            return false;

        Optional<DataObjectFormat> rootOpt = linkRepository.findByAlias(parts[0]);
        if (rootOpt.isEmpty()) {
            logger.warn("Root alias not found for delete: {}", parts[0]);
            return false;
        }

        if (parts.length == 1) {
            linkRepository.deleteById(rootOpt.get().getId());
            logger.info("Deleted root alias: {}", parts[0]);
            return true;
        }

        DataObjectFormat root = rootOpt.get();
        List<DataObjectFormat> currentLevel = root.getSublinks();
        DataObjectFormat parent = null;
        DataObjectFormat toDelete = null;

        for (int i = 1; i < parts.length; i++) {
            String currentAlias = parts[i];

            if (currentLevel == null)
                return false;

            Optional<DataObjectFormat> match = currentLevel.stream()
                    .filter(link -> link.getAlias().equals(currentAlias))
                    .findFirst();

            if (match.isEmpty())
                return false;

            parent = toDelete;
            toDelete = match.get();

            if (i < parts.length - 1) {
                currentLevel = toDelete.getSublinks();
            }
        }

        if (toDelete != null && parent != null && parent.getSublinks() != null) {
            parent.getSublinks().remove(toDelete);
        } else if (toDelete != null && root.getSublinks() != null) {
            root.getSublinks().remove(toDelete);
        }

        linkRepository.save(root); // persist the updated root tree
        logger.info("Deleted sublink: {}", aliasPath);
        return true;
    }

    public boolean aliasExists(String aliasPath) {
        String[] parts = aliasPath.split("/");

        if (parts.length == 0)
            return false;

        Optional<DataObjectFormat> rootOpt = linkRepository.findByAlias(parts[0]);
        if (rootOpt.isEmpty())
            return false;

        if (parts.length == 1) {
            return true; // Root alias exists
        }

        DataObjectFormat current = traverseSublinks(rootOpt.get().getSublinks(),
                Arrays.copyOfRange(parts, 1, parts.length));
        return current != null;
    }

}