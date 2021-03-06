package ib.project.security.authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import ib.security.TokenUtils;



//Filter koji ce presretati svaki zahtev klijenta ka serveru
//Sem nad putanjama navedenim u WebSecurityConfig.configure(WebSecurity web)
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final Log logger = LogFactory.getLog(this.getClass());

  private TokenUtils tokenUtils;

  private UserDetailsService userDetailsService;

  public TokenAuthenticationFilter(TokenUtils tokenUtils, UserDetailsService userDetailsService) {
      this.tokenUtils = tokenUtils;
      this.userDetailsService = userDetailsService;
  }


  @Override
  public void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain chain
  ) throws IOException, ServletException {

      String username;
      String authToken = tokenUtils.getToken(request);

      if (authToken != null) {
          //uzmi username iz tokena
          username = tokenUtils.getUsernameFromToken(authToken);
          if (username != null) {
              // uzmi user-a na osnovu username-a
              UserDetails userDetails = userDetailsService.loadUserByUsername(username);
              //proveri da li je prosledjeni token validan
              if (tokenUtils.validateToken(authToken, userDetails)) {
                  // kreiraj autentifikaciju
                  TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                  authentication.setToken(authToken);
                  SecurityContextHolder.getContext().setAuthentication(authentication);
              }
          }
      }
      chain.doFilter(request, response);
  }

}
