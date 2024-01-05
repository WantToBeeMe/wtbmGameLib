//DEFAULT FRAGMENT SHADER
#version 460 core

#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D textureSampler;

in vec2 fragTexCoord;
in vec4 fragColor;

out vec4 color;
void main() {
    vec4 texelColor = texture(textureSampler, fragTexCoord);
    color = fragColor*texelColor;
}