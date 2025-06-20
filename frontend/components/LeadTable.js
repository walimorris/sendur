import * as React from 'react';
import PropTypes from 'prop-types';
import { alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Checkbox from '@mui/material/Checkbox';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import DeleteIcon from '@mui/icons-material/Delete';
import SendIcon from '@mui/icons-material/Send';
import FilterListIcon from '@mui/icons-material/FilterList';
import { visuallyHidden } from '@mui/utils';
import axios from "axios";
import {useEffect} from "react";
import {Button} from "@mui/material";

function descendingComparator(a, b, orderBy) {
    if (b[orderBy] < a[orderBy]) {
        return -1;
    }
    if (b[orderBy] > a[orderBy]) {
        return 1;
    }
    return 0;
}

function getComparator(order, orderBy) {
    return order === 'desc'
        ? (a, b) => descendingComparator(a, b, orderBy)
        : (a, b) => -descendingComparator(a, b, orderBy);
}

const headCells = [
    {
        id: 'businessName',
        numeric: false,
        disablePadding: true,
        label: 'Business Name',
    },
    {
        id: 'phone',
        numeric: false,
        disablePadding: false,
        label: 'Phone',
    },
    {
        id: 'email',
        numeric: false,
        disablePadding: false,
        label: 'Email',
    },
    {
        id: 'city',
        numeric: false,
        disablePadding: false,
        label: 'City',
    },
    {
        id: 'website',
        numeric: false,
        disablePadding: false,
        label: 'Website',
    },
    {
        id: 'emailDraft',
        numeric: false,
        disablePadding: false,
        label: 'Email Draft',
    },
    {
        id: 'haveContacted',
        numeric: false,
        disablePadding: false,
        label: 'Have Contacted',
    },
];

function EnhancedTableHead(props) {
    const { onSelectAllClick, order, orderBy, numSelected, rowCount, onRequestSort } =
        props;
    const createSortHandler = (property) => (event) => {
        onRequestSort(event, property);
    };

    return (
        <TableHead>
            <TableRow>
                <TableCell padding="checkbox">
                    <Checkbox
                        color="primary"
                        indeterminate={numSelected > 0 && numSelected < rowCount}
                        checked={rowCount > 0 && numSelected === rowCount}
                        onChange={onSelectAllClick}
                        inputProps={{
                            'aria-label': 'select all desserts',
                        }}
                    />
                </TableCell>
                {headCells.map((headCell) => (
                    <TableCell
                        key={headCell.id}
                        align={headCell.numeric ? 'right' : 'left'}
                        padding={headCell.disablePadding ? 'none' : 'normal'}
                        sortDirection={orderBy === headCell.id ? order : false}
                    >
                        <TableSortLabel
                            active={orderBy === headCell.id}
                            direction={orderBy === headCell.id ? order : 'asc'}
                            onClick={createSortHandler(headCell.id)}
                        >
                            {headCell.label}
                            {orderBy === headCell.id ? (
                                <Box component="span" sx={visuallyHidden}>
                                    {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                                </Box>
                            ) : null}
                        </TableSortLabel>
                    </TableCell>
                ))}
            </TableRow>
        </TableHead>
    );
}

EnhancedTableHead.propTypes = {
    numSelected: PropTypes.number.isRequired,
    onRequestSort: PropTypes.func.isRequired,
    onSelectAllClick: PropTypes.func.isRequired,
    order: PropTypes.oneOf(['asc', 'desc']).isRequired,
    orderBy: PropTypes.string.isRequired,
    rowCount: PropTypes.number.isRequired,
};

function EnhancedTableToolbar({numSelected, selected}) {
    async function handleSendLeads() {
        try {
            const response = await axios.post("/sendur/api/leads/approve-lead-emails", selected, {
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            console.log("Successfully sent leads: ", response.data);
        } catch (err) {
            console.log("Failed to send selected leads", err);
        }
    }
    return (
        <Toolbar
            sx={[
                {
                    pl: { sm: 2 },
                    pr: { xs: 1, sm: 1 },
                },
                numSelected > 0 && {
                    bgcolor: (theme) =>
                        alpha(theme.palette.primary.main, theme.palette.action.activatedOpacity),
                },
            ]}
        >
            {numSelected > 0 ? (
                <Typography
                    sx={{ flex: '1 1 100%' }}
                    color="inherit"
                    variant="subtitle1"
                    component="div"
                >
                    {numSelected} selected
                </Typography>
            ) : (
                <Typography
                    sx={{ flex: '1 1 100%' }}
                    variant="h6"
                    id="tableTitle"
                    component="div"
                >
                    Select
                </Typography>
            )}
            {numSelected > 0 ? (
                // <Tooltip title="Delete">
                //     <IconButton>
                //         <DeleteIcon />
                //     </IconButton>
                // </Tooltip>
                <Tooltip title="Send">
                    <IconButton onClick={handleSendLeads}>
                        <SendIcon />
                    </IconButton>
                </Tooltip>
            ) : (
                <Tooltip title="Filter list">
                    <IconButton>
                        <FilterListIcon />
                    </IconButton>
                </Tooltip>
            )}
        </Toolbar>
    );
}

EnhancedTableToolbar.propTypes = {
    numSelected: PropTypes.number.isRequired,
    selected: PropTypes.array.isRequired
};

export default function LeadTable() {
    const [leads, setLeads] = React.useState([]);
    const [order, setOrder] = React.useState('asc');
    const [orderBy, setOrderBy] = React.useState('city');
    const [selected, setSelected] = React.useState([]);
    const [page, setPage] = React.useState(0);
    const [dense, setDense] = React.useState(false);
    const [rowsPerPage, setRowsPerPage] = React.useState(25);

    useEffect(() => {
        const cached = sessionStorage.getItem("leads");
        if (cached) {
            setLeads(JSON.parse(cached));
            console.log("Loading from cache....");
            console.log(cached);
        } else {
            handleLoadAllLeads();
        }
    }, []);

    async function handleLoadAllLeads() {
        try {
            const response = await axios.get(`/sendur/api/leads/find-all`, {
                timeout: 3000,
                signal: AbortSignal.timeout(6000),
            });
            setLeads(response.data);
            sessionStorage.setItem("leads", JSON.stringify(response.data));
            console.log("Loading from datastore...");
            console.log(response.data);
        } catch (err) {
            console.error(err);
        }
    }

    const handleRequestSort = (event, property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleSelectAllClick = (event) => {
        if (event.target.checked) {
            const newSelected = leads.map((n) => n._id);
            setSelected(newSelected);
            return;
        }
        setSelected([]);
    };

    const handleClick = (event, lead) => {
        const selectedIndex = selected.findIndex(item => item._id === lead._id);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = [...selected, lead]; // add the lead
        } else {
            newSelected = [
                ...selected.slice(0, selectedIndex),
                ...selected.slice(selectedIndex + 1)
            ]; // remove the lead
        }
        setSelected(newSelected);
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleChangeDense = (event) => {
        setDense(event.target.checked);
    };

    // Avoid a layout jump when reaching the last page with empty rows.
    const emptyRows =
        page > 0 ? Math.max(0, (1 + page) * rowsPerPage - leads.length) : 0;

    const visibleRows = React.useMemo(
        () =>
            [...leads]
                .sort(getComparator(order, orderBy))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage),
        [leads, order, orderBy, page, rowsPerPage],
    );

    return (
        <Box sx={{ width: '90%', mx: 'auto'}}>
            <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 2}}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <img src="/images/sendur_worm_tiny.png" alt="Sendur Logo" style={{ height: 40, marginRight: 12 }}/>
                    <Typography variant="h6">Sendur Lead Contact Automation</Typography>
                </Box>
                <Button
                    variant="outlined"
                    color="error"
                    onClick={() => window.location.href = '/logout'}
                >
                    Logout
                </Button>
            </Box>
            <Paper sx={{width: '100%', mb: 2}}>
                <EnhancedTableToolbar numSelected={selected.length} selected={selected}/>
                <TableContainer>
                    <Table
                        sx={{minWidth: 750}}
                        aria-labelledby="tableTitle"
                        size={dense ? 'small' : 'medium'}
                    >
                        <EnhancedTableHead
                            numSelected={selected.length}
                            order={order}
                            orderBy={orderBy}
                            onSelectAllClick={handleSelectAllClick}
                            onRequestSort={handleRequestSort}
                            rowCount={leads.length}
                        />
                        <TableBody>
                            {visibleRows.map((lead, index) => {
                                const isItemSelected = selected.some((item) => item._id === lead._id);
                                const labelId = `enhanced-table-checkbox-${index}`;

                                return (
                                    <TableRow
                                        hover
                                        onClick={(event) => handleClick(event, lead)}
                                        role="checkbox"
                                        aria-checked={isItemSelected}
                                        tabIndex={-1}
                                        key={lead._id}
                                        selected={isItemSelected}
                                        sx={{cursor: 'pointer'}}
                                    >
                                        <TableCell padding="checkbox">
                                            <Checkbox
                                                color="primary"
                                                checked={isItemSelected}
                                                inputProps={{
                                                    'aria-labelledby': labelId,
                                                }}
                                            />
                                        </TableCell>
                                        <TableCell
                                            component="th"
                                            id={labelId}
                                            scope="row"
                                            padding="none"
                                        >
                                            {lead.businessName}
                                        </TableCell>
                                        <TableCell align="right">{lead.phone}</TableCell>
                                        <TableCell align="right">{lead.email}</TableCell>
                                        <TableCell align="right">{lead.city}</TableCell>
                                        <TableCell align="right">{lead.website}</TableCell>
                                        <TableCell align="right">{lead.emailDraft}</TableCell>
                                        <TableCell align="right">{lead.haveContacted === true ? 'Yes' : 'No'}</TableCell>
                                    </TableRow>
                                );
                            })}
                            {emptyRows > 0 && (
                                <TableRow
                                    style={{
                                        height: (dense ? 33 : 53) * emptyRows,
                                    }}
                                >
                                    <TableCell colSpan={8}/>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
                <TablePagination
                    rowsPerPageOptions={[5, 10, 25]}
                    component="div"
                    count={leads.length}
                    rowsPerPage={rowsPerPage}
                    page={page}
                    onPageChange={handleChangePage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                />
            </Paper>
            <FormControlLabel
                control={<Switch checked={dense} onChange={handleChangeDense}/>}
                label="Dense padding"
            />
            <pre>{JSON.stringify(selected, null, 2)}</pre>
        </Box>
    );
}
