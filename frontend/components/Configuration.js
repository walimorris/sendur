import React, {useEffect, useState} from 'react';
import Navigation from "./Navigation";
import {
    Box,
    Typography,
    Autocomplete,
    TextField,
    Card,
    CardContent,
    CardActions,
    Button,
    Divider,
    Chip,
    Stack, TextareaAutosize
} from '@mui/material';
import axios from "axios";

const Configuration = () => {
    const [aiWorkflows, setAiWorkflows] = useState([]);
    const [selectedWorkflow, setSelectedWorkflow] = useState('');
    const [prompts, setPrompts] = useState([]);

    const handlePromptChange = (id, value) => {
        setPrompts(prev =>
            prev.map(agent =>
                agent.id === id ? { ...agent, prompt: value } : agent
            )
        );
    };

    useEffect(() => {
        handleAiWorkFlowList();
    }, []);

    useEffect(() => {
        if (selectedWorkflow) {
            handlePromptFetch(selectedWorkflow.name);
        }
    }, [selectedWorkflow]);

    /**
     * Calls the 'ai-agent-workflow-names' API to generate the list of all possible n8n ai-agent workflows.
     * @returns {Promise<void>}
     */
    async function handleAiWorkFlowList() {
        try {
            const response = await axios.get("/sendur/api/n8n/configuration/ai-agent-workflow-names", {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(res => {
                const asObjects = res.data.map(name => ({ id: name, name }));
                setAiWorkflows(asObjects);
                setSelectedWorkflow(asObjects[0]);
            })
            console.log("Successfully loaded ai workflows: ", response.data);
        } catch (err) {
            console.log("Failed to load ai workflows", err);
        }
    }

    /**
     * Prompts are fetched based on the given workflow. Workflows can have multiple Ai Agents that utilize
     * user prompts to complete tasks. This method passes the workflow name and calls the backend
     * 'receive-prompts' API to generate all prompts from the given workflow.
     *
     * @param workflowName n8n workflow name
     * @returns {Promise<void>}
     */
    async function handlePromptFetch(workflowName) {
        try {
            const res = await axios.get("/sendur/api/n8n/configuration/receive-prompts", {
                params: { workflowName },
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            setPrompts(res.data);
            console.log("Prompts loaded:", res.data);
        } catch (err) {
            console.error("Failed to load prompts", err);
        }
    }

    async function handleSaveWorkflow(workflowAgent) {
        try {
            const res = await axios.post("/sendur/api/n8n/configuration/save-updated-prompt", workflowAgent, {
                withCredentials: true,
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            console.log("Workload saved: " + workflowAgent);
        } catch (err) {
            console.error("Failed to save workload: ", workflowAgent.name, err);
        }
    }

    return (
        <div>
            <Navigation/>
            <Box p={4}>
                <Typography variant="h4" mb={3} fontWeight="bold">
                    Configure AI Prompts
                </Typography>
                <Card variant="outlined" sx={{ borderRadius: 3, mb: 3 }}>
                    <CardContent>
                        <Typography variant="body1" color="text.secondary">
                            Workflows that contain <strong>AI Agent</strong> or <strong>LLM</strong> nodes are automatically detected and their prompts are added to the dropdown list below. Once selected, the corresponding prompt will be displayed and can be customized to fit your needs.
                        </Typography>

                        <Typography variant="body2" color="text.secondary" mt={2}>
                            For example, updating a <strong>Location Agent</strong> prompt may allow you to change the business types it searches for or the geographic region it covers.
                        </Typography>

                        <Typography variant="body2" color="text.secondary" mt={2}>
                            <strong>Note:</strong> Any content wrapped in <code>&lt;json&gt;...&lt;/json&gt;</code> or similar formatting examples is intended to guide the agent’s output and should not be modified. These structures are used to shape the data stored in the database and altering them may break the workflow.
                        </Typography>
                    </CardContent>
                </Card>
                <Box mb={4}>
                    <Autocomplete
                        options={aiWorkflows}
                        getOptionLabel={(option) => option?.name ?? 'select workflow'}
                        value={selectedWorkflow}
                        onChange={(e, newValue) => setSelectedWorkflow(newValue)}
                        renderInput={(params) => (
                            <TextField {...params} label="Select Workflow" variant="outlined" />
                        )}
                    />
                </Box>

                <Stack spacing={3}>
                    {prompts.map(workflowAgent => (
                        <Card key={workflowAgent.id} variant="outlined" sx={{ borderRadius: 3 }}>
                            <CardContent>
                                <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
                                    <Typography variant="h6">{workflowAgent.name}</Typography>
                                    <Chip label="AI Agent" color="primary" size="small" />
                                </Stack>

                                <Divider sx={{ mb: 2 }} />

                                <Box
                                    sx={{
                                        backgroundColor: '#f9f9fb',
                                        fontFamily: 'monospace',
                                        fontSize: '0.875rem',
                                        padding: 2,
                                        borderRadius: 2,
                                        whiteSpace: 'pre-wrap',
                                        overflowX: 'auto',
                                        border: '1px solid #e0e0e0',
                                        lineHeight: 1.6,
                                    }}
                                >
                                    <TextareaAutosize
                                        value={workflowAgent.prompt}
                                        onChange={(e) => handlePromptChange(workflowAgent.id, e.target.value)}
                                        minRows={6}
                                        style={{
                                            width: '100%',
                                            border: 'none',
                                            outline: 'none',
                                            backgroundColor: 'transparent',
                                            fontFamily: 'inherit',
                                            fontSize: 'inherit',
                                            lineHeight: 'inherit',
                                            resize: 'vertical',
                                        }}
                                    />
                                </Box>
                            </CardContent>

                            <CardActions sx={{ justifyContent: 'flex-end', paddingX: 2, paddingBottom: 2 }}>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    onClick={() => handleSaveWorkflow(workflowAgent)}
                                >
                                    Save
                                </Button>
                            </CardActions>
                        </Card>
                    ))}
                </Stack>
            </Box>
        </div>
    );
}

export default Configuration;