import React, {useEffect, useState} from 'react';
import Navigation from "./Navigation";
import axios from 'axios';
import Typography from "@mui/material/Typography";
import UploadFileIcon from '@mui/icons-material/UploadFile';
import Box from "@mui/material/Box";
import {Button, Card, CardActions, CardContent, Chip, Divider, Stack, TextField} from "@mui/material";
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism';


const Workflows = () => {
    const [workflow, setWorkflow] = useState('');
    const [fileName, setFileName] = useState('');

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        setFileName(file?.name || '');

        if (file && file.type === "application/json") {
            const reader = new FileReader();
            reader.onload = (e) => {
                try {
                    const parsed = JSON.parse(e.target.result);
                    setWorkflow(JSON.stringify(parsed, null, 2));
                } catch (err) {
                    alert("invalid json file");
                }
            };
            reader.readAsText(file);
        } else {
            alert("Please upload a valid json file.");
        }
    }

    const handleUpload = async () => {
        try {
            const parsedJson = JSON.parse(workflow);
            await axios.post('/sender/api/n8n/workflow/upload', parsedJson);
        } catch (err) {
            console.log(err);
        }
    }

    return (
        <div>
            <Navigation/>
            <Box p={4}>
                <Typography variant="h4" mb={3} fontWeight="bold">
                    Upload Workflows
                </Typography>
                <Card variant="outlined" sx={{ borderRadius: 3, mb: 3 }}>
                    <CardContent>
                        <Typography variant="body1" color="text.secondary">
                            You can upload exported <strong>n8n workflow JSON</strong> files here. To export a workflow, go to the n8n UI,
                            open your workflow, and select <strong>Export</strong> → <strong>Download as file</strong>. Once uploaded,
                            the workflow configuration will be stored in MongoDB.
                        </Typography>
                        <Typography variant="body2" color="text.secondary" mt={2}>
                            <strong>Note:</strong> Sensitive values like API keys, credentials, and environment variables are not included in the exported file and must be configured separately.
                        </Typography>
                    </CardContent>
                </Card>
                <Stack spacing={3}>
                    <Card key="workflow-upload" variant="outlined" sx={{ borderRadius: 3 }}>
                        <CardContent>
                            <Stack direction="row" alignItems="center" justifyContent="space-between" mb={1}>
                                <Typography variant="h6">Workflow Upload</Typography>
                                <Chip label={fileName || "No file selected"} color="primary" size="small" />
                            </Stack>

                            <Divider sx={{ mb: 2 }} />

                            <Button
                                variant="outlined"
                                component="label"
                                startIcon={<UploadFileIcon />}
                                sx={{ mb: 2 }}
                            >
                                Select Workflow
                                <input
                                    type="file"
                                    hidden
                                    accept=".json"
                                    onChange={handleFileChange}
                                />
                            </Button>

                            <Box sx={{ borderRadius: 2, overflow: 'auto', border: '1px solid #ccc', backgroundColor: '#282c34' }}>
                                <SyntaxHighlighter
                                    language="json"
                                    style={oneDark}
                                    customStyle={{
                                        margin: 0,
                                        padding: '16px',
                                        fontSize: '0.85rem',
                                    }}
                                >
                                    {workflow || '{}'}
                                </SyntaxHighlighter>
                            </Box>
                        </CardContent>

                        <CardActions sx={{ justifyContent: 'flex-end', paddingX: 2, paddingBottom: 2 }}>
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleUpload}
                                disabled={!workflow}
                            >
                                Upload
                            </Button>
                        </CardActions>
                    </Card>
                </Stack>
            </Box>
        </div>
    )
}

export default Workflows;