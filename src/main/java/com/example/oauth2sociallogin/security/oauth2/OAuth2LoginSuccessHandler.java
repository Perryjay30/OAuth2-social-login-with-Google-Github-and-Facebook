package com.example.oauth2sociallogin.security.oauth2;

import com.example.oauth2sociallogin.exceptions.CrustInterviewProjectException;
import com.example.oauth2sociallogin.user.data.model.AuthProvider;
import com.example.oauth2sociallogin.user.data.model.User;
import com.example.oauth2sociallogin.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOauth2User oauth2User = (CustomOauth2User)authentication.getPrincipal();
        String emailAddress = oauth2User.getEmail();
        String firstName = oauth2User.getName();
        Optional<User> user = this.userRepository.findByEmailAddress(emailAddress);
        if (user.isEmpty()) {
            this.createNewUserAfterOAuthLoginSuccess(emailAddress, firstName);
        } else {
            this.updateUserAfterOAuthLoginSuccess(emailAddress, firstName);
        }

        System.out.println("User's emailAddress: " + emailAddress);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void updateUserAfterOAuthLoginSuccess(String emailAddress, String firstName) {
        User existingUser = (User)this.userRepository.findByEmailAddress(emailAddress).orElseThrow(() ->
            new CrustInterviewProjectException("User not found!!"));
        existingUser.setAuthProvider(AuthProvider.GOOGLE);
        existingUser.setFirstName(firstName);
        this.userRepository.save(existingUser);
    }

    private void createNewUserAfterOAuthLoginSuccess(String emailAddress, String firstName) {
        User user = new User();
        user.setEmailAddress(emailAddress);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setFirstName(firstName);
        this.userRepository.save(user);
    }
}