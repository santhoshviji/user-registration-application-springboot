    package com.Springcrud.Registration.service;

    import com.Springcrud.Registration.model.User;
    import com.Springcrud.Registration.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.Optional;
    import java.util.Set;

    @Service
    public class UserService {

        @Autowired
        private UserRepository userRepository;

        private final String uploadDir = "uploads/";

        public Page<User> getAllUsers(Pageable pageable) {
            return userRepository.findAll(pageable);
        }

        public Optional<User> getUserById(Long id) {
            return userRepository.findById(id);
        }

        public User saveUser(User user, MultipartFile profilePicture, MultipartFile supportingDocument) throws IOException {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String profilePicPath = saveFile(profilePicture);
                user.setProfilePicturePath(profilePicPath);
            }

            if (supportingDocument != null && !supportingDocument.isEmpty()) {
                String docPath = saveFile(supportingDocument);
                user.setSupportingDocumentPath(docPath);
            }

            return userRepository.save(user);
        }

        public Page<User> searchUsers(String keyword, Pageable pageable) {
            return userRepository.searchUsersAcrossFields(keyword, pageable);
        }

        public User updateUser(Long id, User updatedUser, MultipartFile profilePicture, MultipartFile supportingDocument) throws IOException {
            Optional<User> existingUserOpt = userRepository.findById(id);

            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();

                existingUser.setName(updatedUser.getName());
                existingUser.setDescription(updatedUser.getDescription());
                existingUser.setGender(updatedUser.getGender());
                existingUser.setCountry(updatedUser.getCountry());
                existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
                existingUser.setSkills(updatedUser.getSkills()); // Update skills

                if (profilePicture != null && !profilePicture.isEmpty()) {
                    String profilePicPath = saveFile(profilePicture);
                    existingUser.setProfilePicturePath(profilePicPath);
                }

                if (supportingDocument != null && !supportingDocument.isEmpty()) {
                    String docPath = saveFile(supportingDocument);
                    existingUser.setSupportingDocumentPath(docPath);
                }

                return userRepository.save(existingUser);
            }

            throw new RuntimeException("User not found");
        }

        public void deleteUser(Long id) {
            userRepository.deleteById(id);
        }

        private String saveFile(MultipartFile file) throws IOException {
            Path path = Paths.get(uploadDir + file.getOriginalFilename());
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return path.toString();
        }
    }
