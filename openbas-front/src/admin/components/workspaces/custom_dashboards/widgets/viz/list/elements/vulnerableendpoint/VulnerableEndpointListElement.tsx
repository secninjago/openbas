import { DevicesOtherOutlined } from '@mui/icons-material';
import { ListItemButton, ListItemIcon, ListItemText, Tooltip } from '@mui/material';
import qs from 'qs';
import { Link } from 'react-router';
import { makeStyles } from 'tss-react/mui';

import AssetPlatformFragment from '../../../../../../../../../components/common/list/fragments/AssetPlatformFragment';
import AssetTagsFragment from '../../../../../../../../../components/common/list/fragments/AssetTagsFragment';
import EndpointActiveFragment from '../../../../../../../../../components/common/list/fragments/EndpointActiveFragment';
import EndpointAgentsPrivilegeFragment
  from '../../../../../../../../../components/common/list/fragments/EndpointAgentsPrivilegeFragment';
import EndpointArchFragment from '../../../../../../../../../components/common/list/fragments/EndpointArchFragment';
import VulnerableEndpointActionFragment
  from '../../../../../../../../../components/common/list/fragments/VulnerableEndpointActionFragment';
import { buildSearchPagination } from '../../../../../../../../../components/common/queryable/QueryableUtils';
import useBodyItemsStyles from '../../../../../../../../../components/common/queryable/style/style';
import { SIMULATION_BASE_URL } from '../../../../../../../../../constants/BaseUrls';
import { type EsVulnerableEndpoint } from '../../../../../../../../../utils/api-types';
import EndpointListItemFragments from '../../../../../../../common/endpoints/EndpointListItemFragments';
import buildStyles from '../ColumnStyles';
import VulnerableEndpointElementStyles from './VulnerableEndpointElementStyles';

const useStyles = makeStyles()(() => ({
  itemHead: { textTransform: 'uppercase' },
  item: { height: 50 },
}));

type Props = {
  columns: string[];
  element: EsVulnerableEndpoint;
};

const VulnerableEndpointListElement = (props: Props) => {
  const { classes } = useStyles();
  const bodyItemsStyles = useBodyItemsStyles();

  const craftedFilter = btoa(qs.stringify({
    ...buildSearchPagination({
      filterGroup: {
        mode: 'and',
        filters: [
          {
            key: 'finding_assets',
            operator: 'eq',
            mode: 'or',
            values: [props.element.vulnerable_endpoint_id ?? ''],
          },
          {
            key: 'finding_type',
            operator: 'eq',
            mode: 'or',
            values: ['CVE'],
          },
        ],
      },
    }),
    key: `simulation-findings_${props.element.base_simulation_side}`,
  }, { allowEmptyArrays: true }));
  const findingsTabUrl = `${SIMULATION_BASE_URL}/${props.element.base_simulation_side}/findings?query=${craftedFilter}`;

  /* eslint-disable react/display-name */
  // eslint doesn't seem to be able to infer the display names of subcomponents but react can
  const elementsFromColumn = (column: string) => {
    switch (column) {
      case EndpointListItemFragments.VULNERABLE_ENDPOINT_PLATFORM:
        return (endpoint: EsVulnerableEndpoint) => <AssetPlatformFragment platform={endpoint.vulnerable_endpoint_platform} />;
      case EndpointListItemFragments.VULNERABLE_ENDPOINT_ARCHITECTURE:
        return (endpoint: EsVulnerableEndpoint) => <EndpointArchFragment arch={endpoint.vulnerable_endpoint_architecture} />;
      case EndpointListItemFragments.VULNERABLE_ENDPOINT_AGENTS_ACTIVE_STATUS:
        return (endpoint: EsVulnerableEndpoint) => <EndpointActiveFragment activity_map={endpoint.vulnerable_endpoint_agents_active_status} />;
      case EndpointListItemFragments.VULNERABLE_ENDPOINT_AGENTS_PRIVILEGES:
        return (endpoint: EsVulnerableEndpoint) => <EndpointAgentsPrivilegeFragment privileges={endpoint.vulnerable_endpoint_agents_privileges} />;
      case EndpointListItemFragments.VULNERABLE_ENDPOINT_ACTION:
        return (endpoint: EsVulnerableEndpoint) => <VulnerableEndpointActionFragment action={endpoint.vulnerable_endpoint_action} />;
      case EndpointListItemFragments.BASE_TAGS_SIDE:
        return (endpoint: EsVulnerableEndpoint) => <AssetTagsFragment tags={endpoint.base_tags_side ?? []} />;
      default: return (endpoint: EsVulnerableEndpoint) => {
        const key = column as keyof typeof endpoint;
        const text = endpoint[key]?.toString() || '';
        return (
          <Tooltip title={text} placement="bottom-start">
            <span>{text}</span>
          </Tooltip>
        );
      };
    }
  };
  /* eslint-enable react/display-name */

  return (
    <ListItemButton
      component={Link}
      to={findingsTabUrl}
      classes={{ root: classes.item }}
      className="noDrag"
    >
      <ListItemIcon>
        <DevicesOtherOutlined color="primary" />
      </ListItemIcon>
      <ListItemText
        primary={(
          <div style={bodyItemsStyles.bodyItems}>
            {props.columns.map(col => (
              <div
                key={col}
                style={{
                  ...bodyItemsStyles.bodyItem,
                  ...buildStyles(props.columns, VulnerableEndpointElementStyles)[col],
                }}
              >
                {elementsFromColumn(col)(props.element)}
              </div>
            ))}
          </div>
        )}
      />
    </ListItemButton>
  );
};

export default VulnerableEndpointListElement;
