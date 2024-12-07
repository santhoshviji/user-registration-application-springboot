package com.Springcrud.Registration.controller;

import com.Springcrud.Registration.model.User;
import com.Springcrud.Registration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    // Get all users with pagination and optional search
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<User> pagedResult = (search == null || search.isEmpty())
                ? userService.getAllUsers(pageable)
                : userService.searchUsers(search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", pagedResult.getNumber() + 1);
        response.put("totalPages", pagedResult.getTotalPages());
        response.put("totalItems", pagedResult.getTotalElements());
        response.put("currentPageItemCount", pagedResult.getNumberOfElements());
        response.put("users", pagedResult.getContent());

        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String gender,
            @RequestParam String country,
            @RequestParam String dateOfBirth,
            @RequestParam Set<String> skills,
            @RequestParam(required = false) MultipartFile profilePicture,
            @RequestParam(required = false) MultipartFile supportingDocument) {
        try {
            User user = new User();
            user.setName(name);
            user.setDescription(description);
            user.setGender(gender);
            user.setCountry(country);
            user.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
            user.setSkills(skills);

            userService.saveUser(user, profilePicture, supportingDocument);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "User registered successfully!");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error registering on user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String gender,
            @RequestParam String country,
            @RequestParam String dateOfBirth,
            @RequestParam Set<String> skills,
            @RequestParam(required = false) MultipartFile profilePicture,
            @RequestParam(required = false) MultipartFile supportingDocument) {
        try {
            User updatedUser = new User();
            updatedUser.setName(name);
            updatedUser.setDescription(description);
            updatedUser.setGender(gender);
            updatedUser.setCountry(country);
            updatedUser.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
            updatedUser.setSkills(skills);

            userService.updateUser(id, updatedUser, profilePicture, supportingDocument);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.OK.value());
            response.put("message", "User updated successfully!");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // Serve files (Profile picture or Supporting document)
    @GetMapping("/files/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename, @RequestParam(defaultValue = "false") boolean forceDownload) {
        try {
            Path filePath = Paths.get("uploads").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
            }

            String contentType = Files.probeContentType(filePath);

            // Default to octet-stream if content type is not found
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Define the Content-Disposition
            String contentDisposition;

            // If forceDownload is true, force the file to be downloaded (e.g., Save As dialog)
            if (forceDownload) {
                contentDisposition = "attachment; filename=\"" + resource.getFilename() + "\"";
            } else {
                // If content is PDF or image, show inline (e.g., PDFs should open in the browser)
                if (contentType.equals("application/pdf") || contentType.startsWith("image/")) {
                    contentDisposition = "inline; filename=\"" + resource.getFilename() + "\"";
                } else {
                    // For other file types, force download
                    contentDisposition = "attachment; filename=\"" + resource.getFilename() + "\"";
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while serving the file.");
        }
    }


}
