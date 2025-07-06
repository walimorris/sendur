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
    Stack
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

    return (
        <div>
            <Navigation/>
            <Box p={4}>
                <Typography variant="h4" mb={3} fontWeight="bold">
                    Configure AI Agent Prompts
                </Typography>

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
                    {prompts.map(agent => (
                        <Card key={agent.id} variant="outlined" sx={{ borderRadius: 3 }}>
                            <CardContent>
                                <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
                                    <Typography variant="h6">{agent.name}</Typography>
                                    <Chip label="AI Agent" color="primary" size="small" />
                                </Stack>

                                <Divider sx={{ mb: 2 }} />

                                <TextField
                                    label="Prompt"
                                    variant="outlined"
                                    fullWidth
                                    multiline
                                    minRows={4}
                                    value={agent.prompt}
                                    onChange={(e) => handlePromptChange(agent.id, e.target.value)}
                                />
                            </CardContent>

                            <CardActions sx={{ justifyContent: 'flex-end', paddingX: 2, paddingBottom: 2 }}>
                                <Button
                                    variant="contained"
                                    color="primary"
                                    onClick={() => console.log(`Save prompt for ${agent.name}`)}
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