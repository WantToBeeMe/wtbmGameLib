//DEFAULT FRAGMENT SHADER
#version 460 core

#ifdef GL_ES
precision mediump float;
#endif

//uniform sampler2D texture0;
//uniform vec4 colDiffuse;
in vec2 fragTexCoord;
in vec4 fragColor;

out vec4 color;
void main() {
    //vec4 texelColor = texture(texture0, fragTexCoord);
    color = fragColor+fragTexCoord.x+fragTexCoord.y;//*texelColor*colDiffuse;
}