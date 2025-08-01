import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Box, Stack } from '@mui/material';
import Typography from "@mui/material/Typography";

const Navigation = () => {
    const navigate = useNavigate();

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                p: 2,
                borderBottom: '1px solid #ccc',
                backgroundColor: '#fff',
                mb: 3
            }}
        >
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <img src="/images/sendur_worm_tiny.png" alt="Sendur Logo" style={{ height: 40, marginRight: 12 }}/>
                <Typography variant="h6">Sendur Lead Contact Automation</Typography>
                <Stack direction="row" spacing={2} sx={{ ml:4 }}>
                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => navigate('/')}
                    >
                        Leads
                    </Button>
                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => navigate('/workflows')}
                    >
                        Workflows
                    </Button>
                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => navigate('/configuration')}
                    >
                        Node Configuration
                    </Button>
                </Stack>
            </Box>
            <Button
                variant="outlined"
                color="error"
                onClick={() => (window.location.href = '/logout')}
            >
                Logout
            </Button>
        </Box>
    );
};

export default Navigation;