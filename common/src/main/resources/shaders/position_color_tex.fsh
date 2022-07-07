in vec4 vertexColor;
in vec2 texCord;
out vec4 FragColor;

uniform sampler2D Texture;

void main() {
    vec4 color = texture(Texture, texCord) * vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    FragColor = color;
}
