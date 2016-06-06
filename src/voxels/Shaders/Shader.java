/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.Shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author otso
 */
public class Shader {
    
    private int pId;
    private int projectionMatrixLocation;
    private int viewMatrixLocation;
    private int modelMatrixLocation;
    
    public Shader(){
        createShader();
        
    }

    private void createShader() {
        // Load the vertex shader
        int vsId = this.loadShader("C:\\Users\\otso\\Documents\\NetBeansProjects\\TheQuadExample\\src\\resources\\basicShader.vs", GL20.GL_VERTEX_SHADER);
        // Load the fragment shader
        int fsId = this.loadShader("C:\\Users\\otso\\Documents\\NetBeansProjects\\TheQuadExample\\src\\resources\\basicShader.fs", GL20.GL_FRAGMENT_SHADER);

        // Create a new shader program that links both shaders
        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        // Position information will be attribute 0
        GL20.glBindAttribLocation(pId, 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(pId, 1, "in_Color");
        // Textute information will be attribute 2
        GL20.glBindAttribLocation(pId, 2, "in_TextureCoord");

        GL20.glLinkProgram(pId);
        GL20.glValidateProgram(pId);

        // Get matrices uniform locations
        projectionMatrixLocation = GL20.glGetUniformLocation(pId, "projectionMatrix");
        viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
        modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");
    }
    
    private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }

        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShader(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Could not compile shader.");
            System.exit(-1);
        }

        return shaderID;
    }

    public int getpId() {
        return pId;
    }

    public int getProjectionMatrixLocation() {
        return projectionMatrixLocation;
    }

    public int getViewMatrixLocation() {
        return viewMatrixLocation;
    }

    public int getModelMatrixLocation() {
        return modelMatrixLocation;
    }
    
    
    
}
