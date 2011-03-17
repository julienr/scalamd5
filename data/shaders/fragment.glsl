//localTex is bump
uniform sampler2D colorTex;
uniform sampler2D localTex;

void main () {
  vec4 texel0 = texture2D(colorTex, gl_TexCoord[0].st);
  vec4 texel1 = texture2D(localTex, gl_TexCoord[0].st);
  vec4 texColor = texel0*texel1;
  gl_FragColor = texColor;
}
