in vec3 Position;
in vec4 Color;

uniform mat4 ModelView;
uniform mat4 ProjectionView;

out vec4 vertexColor;

void main() {
    gl_Position = ProjectionView * ModelView * vec4(Position, 1.0);
    vertexColor = Color;
}
