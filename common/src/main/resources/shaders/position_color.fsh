in vec4 vertexColor;
out vec4 FragColor;

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    FragColor = color;
}
