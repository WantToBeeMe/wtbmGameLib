//DEFAULT VERTEX SHADER
#version 460 core

#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 uProjMtx;
uniform mat4 uViewMtx;

in vec3 vertexPosition;
in vec4 vertexColor;
in vec2 vertexTexCoord;
in float vertexTexID;

out vec2 fragTexCoord;
out vec4 fragColor;
out float fragTexID;
void main() {
    fragTexCoord = vertexTexCoord;
    fragColor = vertexColor;
    fragTexID = vertexTexID;
    gl_Position = uProjMtx * uViewMtx * vec4(vertexPosition, 1);
}

