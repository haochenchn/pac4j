package org.pac4j.oauth.profile.generic;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import java.util.HashMap;
import java.util.Map;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.core.profile.converter.StringConverter;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.OAuth20Profile;
import org.pac4j.oauth.profile.definition.OAuth20ProfileDefinition;

/**
 * <p>This class is the user profile for generic OAuth2 with appropriate getters.</p>
 * <p>The map of <code>profileAttributes</code> is intended to replate the primary/secondary attributes where
 * the key is the name of the attribute and the value is the path to obtain that attribute from the
 * json resopnse, starting from <code>firstNodePath</code></p>
 *
 * @author Julio Arrebola
 */
public class GenericOAuth20ProfileDefinition extends OAuth20ProfileDefinition<OAuth20Profile> {

    
    private final Map<String,String> profileAttributes = new HashMap<>();
    
    String profileUrl = null;
    String profileMethod = null;
    String firstNodePath = null;
    
    public void setProfileMethod(String value) {
        this.profileMethod = value;
    }

    @Override
    public Verb getProfileVerb() {
        if ("POST".equalsIgnoreCase(profileMethod)) {
            return Verb.POST;
        } else if ("GET".equalsIgnoreCase(profileMethod)) {
            return Verb.GET;
        } else {
            return super.getProfileVerb(); 
        }
    }
    
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public String getProfileUrl(OAuth2AccessToken accessToken, OAuth20Configuration configuration) {
        return profileUrl;
    }

    @Override
    public OAuth20Profile extractUserProfile(String body) throws HttpAction {
        final OAuth20Profile profile = new OAuth20Profile();
        final JsonNode json = JsonHelper.getFirstNode(body, getFirstNodePath());
        if (json != null) {
            for (final String attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, attribute, JsonHelper.getElement(json, attribute));
            }
            for (final String attribute : getSecondaryAttributes()) {
                convertAndAdd(profile, attribute, JsonHelper.getElement(json, attribute));
            }
            for (final Map.Entry<String, String> entry : getProfileAttributes().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                convertAndAdd(profile, key, JsonHelper.getElement(json, value));
            }

        }
        return profile;
    }

    public Map<String, String> getProfileAttributes() {
        return this.profileAttributes;
    }
    
     /**
     * Add an attribute as a primary one and its converter.
     *
     * @param name name of the attribute
     * @param converter converter
     */
    public void profileAttribute(final String name, final AttributeConverter<? extends Object> converter) {
        profileAttribute(name, name, converter);
    }

     /**
     * Add an attribute as a primary one and its converter.
     *
     * @param name name of the attribute
     * @param tag json reference 
     * @param converter converter
     */
    public void profileAttribute(final String name, String tag, final AttributeConverter<? extends Object> converter) {
        profileAttributes.put(name, tag);
        if (converter != null) {
            getConverters().put(name, converter);
        } else {
            getConverters().put(name, new StringConverter());
        }
    }

    public String getFirstNodePath() {
        return firstNodePath;
    }

    public void setFirstNodePath(String firstNodePath) {
        this.firstNodePath = firstNodePath;
    }
    
    

}