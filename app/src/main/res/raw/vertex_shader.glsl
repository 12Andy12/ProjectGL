attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_Matrix;
varying vec4 v_Color;
attribute vec2 a_Texture;
varying vec2 v_Texture;
varying vec3 v_Position;

void main()
{
    v_Position = a_Position.xyz;
    gl_Position = u_Matrix * a_Position;
    gl_PointSize = 20.0;

    v_Color = a_Color;
    v_Texture = a_Texture;
}