//DEFAULT FRAGMENT SHADER
#version 460 core

#ifdef GL_ES
precision mediump float;
#endif


uniform sampler2D texturesSampler[32];

in vec2 fragTexCoord;
in vec4 fragColor;
in float fragTexID;

out vec4 color;
void main() {
    vec4 texelColor;

    if (fragTexID < 0.0) {
        texelColor = vec4(1.0);
    } else {
         int textureIndex = int(fragTexID);
         texelColor = texture(texturesSampler[textureIndex], fragTexCoord);
    }
    color = fragColor * texelColor;
}


