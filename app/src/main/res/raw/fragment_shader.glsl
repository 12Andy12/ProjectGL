precision mediump float;

uniform samplerCube u_TextureUnit;
varying vec3 v_Position;
varying vec4 v_Color;

void main()
{
    gl_FragColor = textureCube(u_TextureUnit, v_Position);
}