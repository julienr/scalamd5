===================
Some random thought
===================

Definitions
-----------
[world space] is the world coordinates system
the [world space] -> [eye space] transformation is given by the modelview matrix
[object space] is relative to a given object.
the [world space] -> [object space] transformation is given by the object rotation/translation matrix
[tangent space] is a space that is defined for each vertex by its tangent, normal and binormal

GLSL Bump mapping
-----------------
The idea of bump mapping is quite simple. You get a texture that encode, for each fragment, the normal for this fragment. 
Then, for each fragment, calculate the angle (or the cosine of the angle) between the normal and the light vector and you get
the amount of diffuse lighting for the fragment.
Since normal maps give the normal for each fragment in [tangent space], we need to build a matrix to transform from [eye space] to [tangent space].
This is needed because we'll transform the light vector from [eye space] to [tangent space].

Normal, tangent, binormal
~~~~~~~~~~~~~~~~~~~~~~~~~
Tangent space is defined, for each vertex, by its normal, tangent and binormal. 
The binormal can be calculated in the vertex shader because it is cross(normal, tangent).
These 3 vectors define the basis for the tangent space. 
We calculate 2 of these vectors (usually normal and tangent) on the CPU for each vertex. The calculation is 
done in [object space].

Vertex shader
*************
So, in the vertex shader, we first need to transform these 2 vectors to [eye space] :

  //Eye space -> Tangent space transformation matrix
  vec3 n = normalize(gl_NormalMatrix*gl_Normal);
  vec3 t = normalize(gl_NormalMatrix*tangent);
  vec3 b = cross(t, n);

Then, since these 3 vectors form the basis for tangent space, we can easily build a [eye space] -> [tangent space] matrix :

  mat3 tbnMatrix = mat3(t.x, b.x, n.x,
                        t.y, b.y, n.y,
                        t.z, b.z, n.z);

Now, assuming we have the light direction in [eye space], we can easily transform it to [tangent space] :

  lightVec = tbnMatrix*lightDir;

This value (light direction in [tangent space]) is forwarded to the fragment shader.

Fragment shader
***************
In the fragment shader, we first have to read the encoded normal for our fragment from the normal map.
This normal is encoded in a texture as 3 floats in [0,1]. Since a normal is in [-1,1], we simply double it and subtract 1 :

  vec3 normal = 2.0*texture2D(localTex, gl_TexCoord[0].st).rgb - 1.0;

Now, we just have to compute the angle between our light vector and our normal. Remember that they are now both in [tangent space] :

  float NdotL = max(dot(normal, l), 0.0);

This value gives us the amount of diffuse light that our fragment should receive. If NdotL = 0, the fragment is shadowed and if it 
NdotL = 1, the fragment is fully lighted.

//TODO: Picture of NdotL ligthing

Light position
~~~~~~~~~~~~~~
For bump mapping to work, we need to have a light somewhere. In our program, we'll have our light position in [world space]. 
Before transferring it to our vertex shader, we have to transform it to [eye space]. There are two ways to do it.

OpenGL Lighting facility
************************
The first solution is to use OpenGL's lighting functions. We simply call glLight to set our light position like this :

  glLight(GL_LIGHT0, GL_POSITION, lightposition)

After this, OpenGL will automatically transform 'lightposition' to [eye space] by multiplying it with the CURRENT modelview matrix. 
We can therefore access it later in our vertex shader like this :

  vec3 viewLightPos = gl_LightSource[0].position.xyz;

The tendency of modern OpenGL is to remove all the specialized functions and just provide a generic shader programming model. Therefore,
opengl lighting is not available in OpenGL ES 2 or WebGL. 

Uniform variable
****************
If we don't have access to OpenGL lighting function, we can simply transfer the light position as a uniform variable. Note that to do this,
we have to transform the light position to [eye space] NOT in the vertex shader, but on the CPU, before calling glUniform. 
The right way to do it is with something like that (assuming we have a camera orientated by a quaternion) :

  glProgram.setUniform("lightPos", camera.getRotation.getConjugate.rotate(light.position-camera.getPosition))

Now, a mistake I made was that I was just transferring the light position in [world space] to the vertex shader :
  
  glProgram.setUniform("lightPos", light.position)

An the, in the vertex shader :

  vec3 viewLightPos = vec3(gl_ModelViewMatrix*lightPos); 

This WON'T WORK because the modelview matrix in the vertex shader will contain transformation (rotation, translation) that are specific 
to the current object being rendered. But our light position was in [world space] and not in [object space]. 
The conclusion is we need to transform the light position by the modelview matrix BEFORE applying object-specific transformations to it.


Shadow Maps
-----------

On the importance of local object transformations
=================================================
When generating the [world] -> [light] matrix (by saving GL_MODELVIEW and GL_PROJECTION when rendering from light POV), do NOT forget that each object also has local transformations (its location and rotation).

Therefore, when rendering an object, all the matrix-related operations (glPushMAtrix, glTranslate, glRotate) should be done on the GL_MODELVIEW AND on the worldToLight matrix (usually the GL_TEXTURE matrix for GL_TEXTURE7)

