import React from 'react'
import { ToggleButton, ToggleButtonGroup } from '@mui/material'

// this interface makes sure that the 'onFilterChange' prop has a string type
interface FilterProps {
    onFilterChange: (filter:string) => void;
}

const Filter: React.FC<FilterProps> = ({ onFilterChange }) => {
    const [alignment, setAlignment] = React.useState('');

    const handleChange = (event: React.MouseEvent<HTMLElement>, newAlignment: string) => {
        if (newAlignment !== null) {
            setAlignment(newAlignment);
            onFilterChange(newAlignment);
        }
    }
  
    return (
        <div>
            <ToggleButtonGroup
                color="primary"
                value={alignment}
                exclusive
                onChange={handleChange}
                aria-label="Platform"
            >
              <ToggleButton value="inheritance" sx={{ textTransform: 'none', width:'115px' }}>Inheritance</ToggleButton>
              <ToggleButton value="implementation" sx={{ textTransform: 'none', width:'115px' }}>Implementation</ToggleButton>
              <ToggleButton value="composition" sx={{ textTransform: 'none', width:'115px' }}>Composition</ToggleButton>
              <ToggleButton value="nofilter" sx={{ textTransform: 'none', width:'115px' }}>None</ToggleButton>
            </ToggleButtonGroup>
        </div>
    );
}

export default Filter
