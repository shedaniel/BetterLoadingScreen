in vec3 Position;
in vec4 Color;
in vec2 Texture;

uniform mat4 ModelView;
uniform mat4 ProjectionView;

out vec4 vertexColor;
out vec2 texCord;

void main() {
    gl_Position = ProjectionView * ModelView * vec4(Position, 1.0);
    vertexColor = Color;
    texCord = Texture;
}
