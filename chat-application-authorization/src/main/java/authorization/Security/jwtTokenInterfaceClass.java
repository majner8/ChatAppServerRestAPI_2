package authorization.Security;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import AuthorizationDTO.TokenDTO;
import authorization.Security.jwtToken.jwtTokenValidationInterface;
import chat_application_commonPart.Config.SecurityConfiguration;
import chat_application_common_Part.Security.SecurityProperties;
import database.User.UserEntity;
import io.jsonwebtoken.UnsupportedJwtException;

public class jwtTokenInterfaceClass {

	
	@Component
	public static class jwtTokenValidationClass implements jwtTokenValidationInterface{
		
		
		@Autowired
		private SecurityProperties securityProperties;
		
		private DecodedJWT verifyToken(String headerName, String tokenPreflix, HttpServletRequest request
				,Algorithm tokenAlgo) {
			// TODO Auto-generated method stub
			String rawToken=request.getHeader(headerName);
			if(rawToken==null) {
				throw new UnsupportedJwtException(null);
			}
			if(!rawToken.startsWith(tokenPreflix)) {
				throw new UnsupportedJwtException(null);
			}
			rawToken=rawToken.replaceFirst(tokenPreflix, "");
			JWT.require(tokenAlgo)
			.build()
			.verify(rawToken);				
			return JWT.decode(rawToken);


		}


		@Override
		public DecodedJWT jwtTokenDeviceIDTokenValidator(HttpServletRequest request) {
			// TODO Auto-generated method stub
			return this.verifyToken(this.securityProperties.getTokenDeviceIdHeaderName(), 
					this.securityProperties.getTokenDeviceIdPreflix(), request, 
					this.securityProperties.getjwtTokenDeviceIDAlgorithm());
			
		}

		@Override
		public DecodedJWT jwtTokenAuthorizationUserTokenValidator(HttpServletRequest request) {
			// TODO Auto-generated method stub
			return this.verifyToken(this.securityProperties.getTokenAuthorizationUserHederName(), 
					this.securityProperties.getTokenAuthorizationUserPreflix(), request, 
					this.securityProperties.getjwtTokenAuthorizationUserAlgorithm());
		
		}




		
		
		
	}
	
	public static class jwtTokengeneratorClass implements jwtToken.jwtTokenGeneratorInterface{
		@Autowired
		private SecurityProperties securityProperties;


		public TokenDTO generateAuthorizationToken(
			
				String deviceID,
				UserEntity userEntity) {
			
			Calendar validUntil=this.securityProperties.getJwtTokenAuthorizationUserDuration();
			JWTCreator.Builder jwtBuilder= 
					JWT.create()
					.withSubject(String.valueOf(userEntity.getUserId()))
					.withIssuedAt(new Date())
					.withClaim(SecurityConfiguration.DeviceIdClaimName, deviceID)
					.withClaim(SecurityConfiguration.VersionClaimName,userEntity.getVersion())
					.withClaim(SecurityConfiguration.userIsActiveClaimName, userEntity.isUserActive())
					
					.withExpiresAt(validUntil.getTime());

			if(!userEntity.isUserActive()) {
				//add userEntity to finish registration
				//user Entity is just as map
				jwtBuilder.withClaim(SecurityConfiguration.userEntityClaimName,userEntity.getValues());
			}
			String jwtToken=jwtBuilder		
					.sign(this.securityProperties.getjwtTokenAuthorizationUserAlgorithm());

			TokenDTO token=new TokenDTO();
			token.setUserActive(userEntity.isUserActive());
			token.setValidUntil(validUntil.getTime());
			token.setToken(jwtToken);
			return token;
		}
		
		public String generateDeviceToken(
				String deviceID) {
			Calendar validUntil=this.securityProperties.getJwtTokenDeviceIdDuration();

			return JWT.create()
					.withSubject(deviceID)
					.withIssuedAt(new Date())
					.withExpiresAt(validUntil.getTime())
					.sign(this.securityProperties.getjwtTokenDeviceIDAlgorithm());
		
		}
		
		
	}
}
