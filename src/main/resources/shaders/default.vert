//DEFAULT VERTEX SHADER
#version 460 core

#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 ProjMtx;

in vec3 vertexPosition;
in vec4 vertexColor;
in vec2 vertexTexCoord;

out vec2 fragTexCoord;
out vec4 fragColor;
void main() {
    fragTexCoord = vertexTexCoord;
    fragColor = vertexColor;
    gl_Position = ProjMtx * vec4(vertexPosition, 1);
}

